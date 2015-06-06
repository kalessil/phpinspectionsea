package com.kalessil.phpStorm.phpInspectionsEA.utils.phpExceptions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.phpDoc.ThrowsResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

final public class CollectPossibleThrowsUtil {
    static public HashSet<PhpClass> collectNestedAndWorkflowExceptions(PsiElement scope, HashSet<PsiElement> processed, @NotNull final ProblemsHolder holder) {
        HashSet<PhpClass> exceptions = new HashSet<PhpClass>();

        /* recursively invoke and analyse nested try-catches checks */
        Collection<Try> tryStatements = PsiTreeUtil.findChildrenOfType(scope, Try.class);
        if (tryStatements.size() > 0) {
            for (Try nestedTry : tryStatements) {
                if (!processed.contains(nestedTry)) {
                    /* process nested workflow */
                    HashSet<PhpClass> nestedTryExceptions = collectNestedAndWorkflowExceptions(nestedTry, processed, holder);
//holder.registerProblem(nestedTry.getFirstChild(), "Nested: " + nestedTryExceptions.toString(), ProblemHighlightType.WEAK_WARNING);
                    /* merge into report */
                    exceptions.addAll(nestedTryExceptions);
                    nestedTryExceptions.clear();
                }
            }
            tryStatements.clear();
        }

        /* process try-catch */
        if (scope instanceof Try) {
            /* extract workflow exceptions */
            HashSet<PhpClass> tryWorkflowExceptions = collectTryWorkflowExceptions((Try) scope, processed, holder);
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
                exceptions.add(newClass);
                processed.add(newExpression);
                continue;
            }

            /* process constructors invocation */
            Method constructor = newClass.getConstructor();
            if (null != constructor) {
                ThrowsResolveUtil.resolveThrownExceptions(constructor, exceptions);
            }
            processed.add(newExpression);
        }
        newExpressions.clear();


        /* process nested calls */
        Collection<MethodReference> calls = PsiTreeUtil.findChildrenOfType(scope, MethodReference.class);
        for (MethodReference call : calls) {
            PsiElement methodResolved = call.resolve();
            if (methodResolved instanceof Method) {
                ThrowsResolveUtil.resolveThrownExceptions((Method) methodResolved, exceptions);
            }
            processed.add(call);
        }
        calls.clear();


        return exceptions;
    }

    static private HashSet<PhpClass> collectTryWorkflowExceptions(Try scope, HashSet<PsiElement> processed, @NotNull final ProblemsHolder holder) {
        HashSet<PhpClass> exceptions = new HashSet<PhpClass>();

        /* resolve try-body */
        HashSet<PhpClass> unhandledInTry = collectNestedAndWorkflowExceptions(scope.getStatement(), processed, holder);

        /* resolve all catches */
        HashSet<PhpClass> unhandledInCatches = new HashSet<PhpClass>();
        for (Catch catchInTry : scope.getCatchClauses()) {
            /* resolve catch-class */
            ClassReference catchClassReference = catchInTry.getExceptionType();
            if (null != catchClassReference && catchClassReference.resolve() instanceof PhpClass) {
                PhpClass caughtClass = (PhpClass) catchClassReference.resolve();
//holder.registerProblem(catchInTry.getFirstChild(), "Catches: " + caughtClass.toString(), ProblemHighlightType.WEAK_WARNING);

                /* inspect what covered */
                HashSet<PhpClass> handledInCurrentCatch = new HashSet<PhpClass>();
                handledInCurrentCatch.add(caughtClass);

                for (PhpClass unhandled : unhandledInTry) {
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
                    unhandledInTry.removeAll(handledInCurrentCatch);
                    handledInCurrentCatch.clear();
                }
            }

            /* resolve catch-body and mark as processed */
            unhandledInCatches.addAll(collectNestedAndWorkflowExceptions(catchInTry, processed, holder));
        }

        /* merge unhandled and catch-produced exceptions into result storage */
        exceptions.addAll(unhandledInTry);
        unhandledInTry.clear();

        exceptions.addAll(unhandledInCatches);
        unhandledInCatches.clear();

        return exceptions;
    }
}
