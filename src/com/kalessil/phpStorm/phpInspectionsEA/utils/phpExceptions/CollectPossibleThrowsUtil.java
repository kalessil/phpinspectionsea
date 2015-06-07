package com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc.ThrowsResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

final public class CollectPossibleThrowsUtil {
    static public HashMap<PhpClass, HashSet<PsiElement>> collectNestedAndWorkflowExceptions(PsiElement scope, HashSet<PsiElement> processed, @NotNull final ProblemsHolder holder) {
        HashMap<PhpClass, HashSet<PsiElement>> exceptions = new HashMap<PhpClass, HashSet<PsiElement>>();

        /* recursively invoke and analyse nested try-catches checks */
        Collection<Try> tryStatements = PsiTreeUtil.findChildrenOfType(scope, Try.class);
        if (tryStatements.size() > 0) {
            for (Try nestedTry : tryStatements) {
                if (!processed.contains(nestedTry)) {
                    /* process nested workflow */
                    HashMap<PhpClass, HashSet<PsiElement>> nestedTryExceptions = collectNestedAndWorkflowExceptions(nestedTry, processed, holder);
//holder.registerProblem(nestedTry.getFirstChild(), "Nested: " + nestedTryExceptions.toString(), ProblemHighlightType.WEAK_WARNING);
                    /* merge into report */
                    for (PhpClass key : nestedTryExceptions.keySet()) {
                        HashSet<PsiElement> expressionsToDispatch = nestedTryExceptions.get(key);
                        if (exceptions.containsKey(key)) {
                            exceptions.get(key).addAll(expressionsToDispatch);
                            expressionsToDispatch.clear();
                        } else {
                            exceptions.put(key, expressionsToDispatch);
                        }
                    }

                    nestedTryExceptions.clear();
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
        for (NewExpression newExpression : newExpressions) {
            /* skip processed */
            if (processed.contains(newExpression)) {
                continue;
            }

            /* skip what can not be resolved */
            ClassReference newClassRef= newExpression.getClassReference();
            if (null == newClassRef || !(newClassRef.resolve() instanceof PhpClass)) {
                processed.add(newExpression);
                continue;
            }
            PhpClass newClass = (PhpClass) newClassRef.resolve();

            /* throws processed */
            if (newExpression.getParent() instanceof PhpThrow) {
                /* put an expression, create container if necessary */
                if (!exceptions.containsKey(newClass)) {
                    exceptions.put(newClass, new HashSet<PsiElement>());
                }
                exceptions.get(newClass).add(newExpression.getParent());

                processed.add(newExpression);
                continue;
            }

            /* process constructors invocation */
            Method constructor = newClass.getConstructor();
            if (null != constructor) {
                /* lookup for annotated exceptions */
                HashSet<PhpClass> constructorExceptions = new HashSet<PhpClass>();
                ThrowsResolveUtil.resolveThrownExceptions(constructor, constructorExceptions);

                /* link expression with each possible exception */
                if (constructorExceptions.size() > 0) {
                    for (PhpClass constructorException : constructorExceptions) {
                        /* put an expression, create container if necessary */
                        if (!exceptions.containsKey(constructorException)) {
                            exceptions.put(constructorException, new HashSet<PsiElement>());
                        }
                        exceptions.get(constructorException).add(newExpression.getParent());
                    }

                    constructorExceptions.clear();
                }
            }
            processed.add(newExpression);
        }
        newExpressions.clear();


        /* process nested calls */
        Collection<MethodReference> calls = PsiTreeUtil.findChildrenOfType(scope, MethodReference.class);
        for (MethodReference call : calls) {
            /* skip processed */
            if (processed.contains(call)) {
                continue;
            }

            PsiElement methodResolved = call.resolve();
            if (methodResolved instanceof Method) {
                /* lookup for annotated exceptions */
                HashSet<PhpClass> methodExceptions = new HashSet<PhpClass>();
                ThrowsResolveUtil.resolveThrownExceptions((Method) methodResolved, methodExceptions);

                /* link expression with each possible exception */
                if (methodExceptions.size() > 0) {
                    for (PhpClass methodException : methodExceptions) {
                        /* put an expression, create container if necessary */
                        if (!exceptions.containsKey(methodException)) {
                            exceptions.put(methodException, new HashSet<PsiElement>());
                        }
                        exceptions.get(methodException).add(call);
                    }

                    methodExceptions.clear();
                }
            }
            processed.add(call);
        }
        calls.clear();


        return exceptions;
    }

    static private HashMap<PhpClass, HashSet<PsiElement>> collectTryWorkflowExceptions(Try scope, HashSet<PsiElement> processed, @NotNull final ProblemsHolder holder) {
        HashMap<PhpClass, HashSet<PsiElement>> exceptions = new HashMap<PhpClass, HashSet<PsiElement>>();

        /* resolve try-body */
        HashMap<PhpClass, HashSet<PsiElement>> unhandledInTry = collectNestedAndWorkflowExceptions(scope.getStatement(), processed, holder);

        /* resolve all catches */
        HashMap<PhpClass, HashSet<PsiElement>> unhandledInCatches = new HashMap<PhpClass, HashSet<PsiElement>>();
        for (Catch catchInTry : scope.getCatchClauses()) {
            /* resolve catch-class */
            ClassReference catchClassReference = catchInTry.getExceptionType();
            if (null != catchClassReference && catchClassReference.resolve() instanceof PhpClass) {
                PhpClass caughtClass = (PhpClass) catchClassReference.resolve();
//holder.registerProblem(catchInTry.getFirstChild(), "Catches: " + caughtClass.toString(), ProblemHighlightType.WEAK_WARNING);

                /* inspect what covered */
                HashSet<PhpClass> handledInCurrentCatch = new HashSet<PhpClass>();
                handledInCurrentCatch.add(caughtClass);

                for (PhpClass unhandled : unhandledInTry.keySet()) {
                    /* each potential exception can be caught as a parent as well */
                    HashSet<PhpClass> unhandledVariants = InterfacesExtractUtil.getCrawlCompleteInheritanceTree(unhandled, true);
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
                        if (unhandledInTry.containsKey(oneHandled)) {
                            unhandledInTry.get(oneHandled).clear();
                            unhandledInTry.remove(oneHandled);
                        }
                    }

                    handledInCurrentCatch.clear();
                }
            }

            /* resolve catch-body and mark as processed */
            HashMap<PhpClass, HashSet<PsiElement>> catchBodyExceptions = collectNestedAndWorkflowExceptions(catchInTry, processed, holder);
            for (PhpClass exceptionInCatch : catchBodyExceptions.keySet()) {
                HashSet<PsiElement> expressionsToDispatch = catchBodyExceptions.get(exceptionInCatch);
                if (unhandledInCatches.containsKey(exceptionInCatch)) {
                    /* merge */
                    unhandledInCatches.get(exceptionInCatch).addAll(expressionsToDispatch);
                    expressionsToDispatch.clear();
                } else {
                    /* put */
                    unhandledInCatches.put(exceptionInCatch, expressionsToDispatch);
                }
            }
            catchBodyExceptions.clear();
        }

        /* merge unhandled and catch-produced exceptions into result storage */
        for (PhpClass tryException : unhandledInTry.keySet()) {
            HashSet<PsiElement> expressionsToDispatch = unhandledInTry.get(tryException);
            if (exceptions.containsKey(tryException)) {
                exceptions.get(tryException).addAll(expressionsToDispatch);
                expressionsToDispatch.clear();
            } else {
                exceptions.put(tryException, expressionsToDispatch);
            }
        }
        unhandledInTry.clear();

        for (PhpClass catchException : unhandledInCatches.keySet()) {
            HashSet<PsiElement> expressionsToDispatch = unhandledInCatches.get(catchException);
            if (exceptions.containsKey(catchException)) {
                exceptions.get(catchException).addAll(expressionsToDispatch);
                expressionsToDispatch.clear();
            } else {
                exceptions.put(catchException, expressionsToDispatch);
            }
        }
        unhandledInCatches.clear();

        return exceptions;
    }
}
