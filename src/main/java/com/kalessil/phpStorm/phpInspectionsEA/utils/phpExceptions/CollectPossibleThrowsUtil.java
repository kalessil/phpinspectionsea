package com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc.ThrowsResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

final public class CollectPossibleThrowsUtil {
    static public HashMap<PhpClass, HashSet<PsiElement>> collectNestedAndWorkflowExceptions(PsiElement scope, HashSet<PsiElement> processed, @NotNull final ProblemsHolder holder) {
        final HashMap<PhpClass, HashSet<PsiElement>> exceptions = new HashMap<>();

        /* recursively invoke and analyse nested try-catches checks */
        final Collection<Try> tryStatements = PsiTreeUtil.findChildrenOfType(scope, Try.class);
        if (tryStatements.size() > 0) {
            for (Try nestedTry : tryStatements) {
                if (!processed.contains(nestedTry)) {
                    /* process nested workflow */
                    final HashMap<PhpClass, HashSet<PsiElement>> nestedTryExceptions = collectNestedAndWorkflowExceptions(nestedTry, processed, holder);
//holder.registerProblem(nestedTry.getFirstChild(), "Nested: " + nestedTryExceptions.toString(), ProblemHighlightType.WEAK_WARNING);
                    if (nestedTryExceptions.size() > 0) {
                        for (final Map.Entry<PhpClass, HashSet<PsiElement>> nestedTryExceptionsPair : nestedTryExceptions.entrySet()) {
                            /* extract pairs Exception class => source expressions */
                            final PhpClass key                              = nestedTryExceptionsPair.getKey();
                            final HashSet<PsiElement> expressionsToDispatch = nestedTryExceptionsPair.getValue();

                            if (exceptions.containsKey(key)) {
                                /* merge entries and release refs */
                                exceptions.get(key).addAll(expressionsToDispatch);
                                expressionsToDispatch.clear();
                            } else {
                                /* store as it is */
                                exceptions.put(key, expressionsToDispatch);
                            }
                        }
                        nestedTryExceptions.clear();
                    }
                }
            }
            tryStatements.clear();
        }

        /* process try-catch */
        if (scope instanceof Try) {
            /* extract workflow exceptions */
            HashMap<PhpClass, HashSet<PsiElement>> tryWorkflowExceptions = collectTryWorkflowExceptions((Try) scope, processed, holder);
//holder.registerProblem(scope.getFirstChild(), "Throws: " + tryWorkflowExceptions.toString(), ProblemHighlightType.WEAK_WARNING);

            /* mark processed and exit, as try-catch handled in special way */
            processed.add(scope);
            exceptions.clear();
            return tryWorkflowExceptions;
        }


        /* process new statements: throws, constructors */
        Collection<NewExpression> newExpressions = PsiTreeUtil.findChildrenOfType(scope, NewExpression.class);
        if (newExpressions.size() > 0) {
            for (NewExpression newExpression : newExpressions) {
                /* skip processed */
                if (processed.contains(newExpression)) {
                    continue;
                }
//holder.registerProblem(newExpression, "New expression wil be analyzed", ProblemHighlightType.WEAK_WARNING);

                /* skip what can not be resolved */
                ClassReference newClassRef = newExpression.getClassReference();
                if (null == newClassRef) {
                    processed.add(newExpression);
                    continue;
                }

                PhpClass newClass;
                final PsiElement resolved = OpenapiResolveUtil.resolveReference(newClassRef);
                if (resolved instanceof PhpClass) {
                    newClass = (PhpClass) resolved;
                } else if (resolved instanceof Method) {
                    newClass = ((Method) resolved).getContainingClass();
                } else {
                    processed.add(newExpression);
                    continue;
                }
//holder.registerProblem(newExpression, "Instantiated class resolved", ProblemHighlightType.WEAK_WARNING);

                /* throws processed */
                if (newExpression.getParent() instanceof PhpThrow) {
                    /* put an expression, create container if necessary */
                    if (!exceptions.containsKey(newClass)) {
                        exceptions.put(newClass, new HashSet<>());
                    }
                    exceptions.get(newClass).add(newExpression.getParent());

                    processed.add(newExpression);
                    continue;
                }

                /* process constructors invocation */
                final Method constructor = newClass == null ? null : newClass.getConstructor();
                if (constructor != null) {
//holder.registerProblem(newExpression, "Constructor found", ProblemHighlightType.WEAK_WARNING);
                    /* lookup for annotated exceptions */
                    final HashSet<PhpClass> constructorExceptions = new HashSet<>();
                    ThrowsResolveUtil.resolveThrownExceptions(constructor, constructorExceptions);

                    /* link expression with each possible exception */
                    if (constructorExceptions.size() > 0) {
                        for (PhpClass constructorException : constructorExceptions) {
                            /* put an expression, create container if necessary */
                            if (!exceptions.containsKey(constructorException)) {
                                exceptions.put(constructorException, new HashSet<>());
                            }
                            exceptions.get(constructorException).add(newExpression.getParent());
                        }

                        constructorExceptions.clear();
                    }
                }
                processed.add(newExpression);
            }
            newExpressions.clear();
        }

        /* process throws - some of them might not use new-expression */
        final Project project   = holder.getProject();
        final PhpIndex objIndex = PhpIndex.getInstance(project);
        Collection<PhpThrow> throwExpressions = PsiTreeUtil.findChildrenOfType(scope, PhpThrow.class);
        if (!throwExpressions.isEmpty()) {
            for (final PhpThrow throwExpression : throwExpressions) {
                /* skip processed */
                if (processed.contains(throwExpression)) {
                    continue;
                }

                /* resolve argument */
                final PsiElement argument = throwExpression.getArgument();
                if (argument instanceof PhpTypedElement) {
                    /* resolve argument types */
                    final HashSet<String> types = new HashSet<>();
                    final PhpType resolved      = OpenapiResolveUtil.resolveType((PhpTypedElement) argument, project);
                    if (resolved != null) {
                        resolved.getTypes().forEach(t -> types.add(Types.getType(t)));
                    }

                    if (!types.isEmpty()) {
                        /* remove extra definition of \Exception unexpectedly added by PhpStorm */
                        final boolean dropExtraDefinitions = argument instanceof Variable && types.size() > 1 && types.contains("\\Exception");
                        if (dropExtraDefinitions) {
                            types.remove("\\Exception");
                        }

                        for (String type : types) {
                            if (type.startsWith("\\")) {
                                /* process classes references */
                                final Collection<PhpClass> classes = OpenapiResolveUtil.resolveClassesByFQN(type, objIndex);
                                if (!classes.isEmpty()) {
                                    /* put an expression, create container if necessary */
                                    final PhpClass exception = classes.iterator().next();
                                    if (!exceptions.containsKey(exception)) {
                                        exceptions.put(exception, new HashSet<>());
                                    }
                                    exceptions.get(exception).add(throwExpression);
                                }
                            }
                        }
                        types.clear();
                    }
                }

                processed.add(throwExpression);
            }
            throwExpressions.clear();
        }

        /* process nested calls */
        Collection<MethodReference> calls = PsiTreeUtil.findChildrenOfType(scope, MethodReference.class);
        if (calls.size() > 0) {
            for (MethodReference call : calls) {
                /* skip processed */
                if (processed.contains(call)) {
                    continue;
                }

                PsiElement methodResolved = OpenapiResolveUtil.resolveReference(call);
                if (methodResolved instanceof Method) {
                    /* lookup for annotated exceptions */
                    final HashSet<PhpClass> methodExceptions = new HashSet<>();
                    ThrowsResolveUtil.resolveThrownExceptions((Method) methodResolved, methodExceptions);

                    /* link expression with each possible exception */
                    if (methodExceptions.size() > 0) {
                        for (PhpClass methodException : methodExceptions) {
                            /* put an expression, create container if necessary */
                            if (!exceptions.containsKey(methodException)) {
                                exceptions.put(methodException, new HashSet<>());
                            }
                            exceptions.get(methodException).add(call);
                        }

                        methodExceptions.clear();
                    }
                }
                processed.add(call);
            }
            calls.clear();
        }

        return exceptions;
    }

    static private HashMap<PhpClass, HashSet<PsiElement>> collectTryWorkflowExceptions(Try scope, HashSet<PsiElement> processed, @NotNull final ProblemsHolder holder) {
        final HashMap<PhpClass, HashSet<PsiElement>> exceptions = new HashMap<>();

        /* resolve try-body */
        final HashMap<PhpClass, HashSet<PsiElement>> unhandledInTry = collectNestedAndWorkflowExceptions(scope.getStatement(), processed, holder);

        /* resolve all catches */
        final HashMap<PhpClass, HashSet<PsiElement>> unhandledInCatches = new HashMap<>();
        for (Catch catchInTry : scope.getCatchClauses()) {
            /* resolve catch-class */
            final Collection<ClassReference> catchClassReferences = catchInTry.getExceptionTypes();
            for (ClassReference catchClassReference : catchClassReferences) {
                final PsiElement resolved = OpenapiResolveUtil.resolveReference(catchClassReference);
                if (!(resolved instanceof PhpClass)) {
                    continue;
                }

                final PhpClass caughtClass = (PhpClass) resolved;
//holder.registerProblem(catchInTry.getFirstChild(), "Catches: " + caughtClass.toString(), ProblemHighlightType.WEAK_WARNING);

                /* inspect what covered */
                final HashSet<PhpClass> handledInCurrentCatch = new HashSet<>();
                handledInCurrentCatch.add(caughtClass);

                for (PhpClass unhandled : unhandledInTry.keySet()) {
                    /* each potential exception can be caught as a parent as well */
                    HashSet<PhpClass> unhandledVariants = InterfacesExtractUtil.getCrawlInheritanceTree(unhandled, true);
                    unhandledVariants.add(unhandled);
//holder.registerProblem(catchInTry.getFirstChild(), "Match against: " + unhandledVariants.toString(), ProblemHighlightType.WEAK_WARNING);
                    if (unhandledVariants.contains(caughtClass)) {
                        handledInCurrentCatch.add(unhandled);
                    }
                    unhandledVariants.clear();
                }

                /* actualize unhandled in try, they will remain here */
                if (handledInCurrentCatch.size() > 0) {
//holder.registerProblem(catchInTry.getFirstChild(), "Exclude thrown: " + handledInCurrentCatch.toString(), ProblemHighlightType.WEAK_WARNING);
                    /* cleanup info associated with covered exceptions */
                    for (PhpClass oneHandled : handledInCurrentCatch) {
//holder.registerProblem(catchInTry.getFirstChild(), "Apply to try: " + unhandledInTry.keySet().toString(), ProblemHighlightType.WEAK_WARNING);
                        if (unhandledInTry.containsKey(oneHandled)) {
                            unhandledInTry.get(oneHandled).clear();
                            unhandledInTry.remove(oneHandled);
                        }
                    }
                    handledInCurrentCatch.clear();
                }
            }
            catchClassReferences.clear();

            /* resolve catch-body and mark as processed */
            final HashMap<PhpClass, HashSet<PsiElement>> catchBodyExceptions = collectNestedAndWorkflowExceptions(catchInTry, processed, holder);
            if (catchBodyExceptions.size() > 0) {
                for (Map.Entry<PhpClass, HashSet<PsiElement>> catchBodyExceptionsPair : catchBodyExceptions.entrySet()) {
                    /* extract pairs Exception class => source expressions */
                    final PhpClass exceptionInCatch                 = catchBodyExceptionsPair.getKey();
                    final HashSet<PsiElement> expressionsToDispatch = catchBodyExceptionsPair.getValue();

                    if (unhandledInCatches.containsKey(exceptionInCatch)) {
                        /* merge entries and release refs */
                        unhandledInCatches.get(exceptionInCatch).addAll(expressionsToDispatch);
                        expressionsToDispatch.clear();
                    } else {
                        /* store as it is */
                        unhandledInCatches.put(exceptionInCatch, expressionsToDispatch);
                    }
//holder.registerProblem(catchInTry.getFirstChild(), "Introduced by catches: " + unhandledInCatches.keySet().toString(), ProblemHighlightType.WEAK_WARNING);
                }
                catchBodyExceptions.clear();
            }
        }

        /* merge unhandled and catch-produced exceptions into result storage */
        if (unhandledInTry.size() > 0) {
            for (Map.Entry<PhpClass, HashSet<PsiElement>> unhandledInTryPair : unhandledInTry.entrySet()) {
                /* extract pairs Exception class => source expressions */
                final PhpClass tryException                     = unhandledInTryPair.getKey();
                final HashSet<PsiElement> expressionsToDispatch = unhandledInTryPair.getValue();

                if (exceptions.containsKey(tryException)) {
                    /* merge entries and release refs */
                    exceptions.get(tryException).addAll(expressionsToDispatch);
                    expressionsToDispatch.clear();
                } else {
                    /* store as it is */
                    exceptions.put(tryException, expressionsToDispatch);
                }
            }
//holder.registerProblem(scope.getFirstChild(), "Try produces: " + unhandledInTry.keySet().toString(), ProblemHighlightType.WEAK_WARNING);
            unhandledInTry.clear();
        }

        if (unhandledInCatches.size() > 0) {
            for (Map.Entry<PhpClass, HashSet<PsiElement>> unhandledInCatchesPair : unhandledInCatches.entrySet()) {
                /* extract pairs Exception class => source expressions */
                final PhpClass catchException                   = unhandledInCatchesPair.getKey();
                final HashSet<PsiElement> expressionsToDispatch = unhandledInCatchesPair.getValue();

                if (exceptions.containsKey(catchException)) {
                    /* merge entries and release refs */
                    exceptions.get(catchException).addAll(expressionsToDispatch);
                    expressionsToDispatch.clear();
                } else {
                    /* store as it is */
                    exceptions.put(catchException, expressionsToDispatch);
                }
            }
//holder.registerProblem(scope.getFirstChild(), "Catches produces: " + unhandledInCatches.keySet().toString(), ProblemHighlightType.WEAK_WARNING);
            unhandledInCatches.clear();
        }

        return exceptions;
    }
}
