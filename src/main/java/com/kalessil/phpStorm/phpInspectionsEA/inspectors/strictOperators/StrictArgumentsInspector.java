package com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictOperators.util.PhpExpressionTypes;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class StrictArgumentsInspector extends BasePhpInspection {
    private static final String strProblemDescriptionArgumentTypeMismatch = "Argument type of (%t1%) doesn't match parameter type of (%t2%).";

    @NotNull
    public String getShortName() {
        return "StrictArgumentsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {

            public void visitPhpMethodReference(MethodReference reference) {
                PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());

                Collection<? extends PhpNamedElement> methods = objIndex.getBySignature(reference.getSignature());
                if (methods.size() != 1) {
                    return;
                }

                PhpNamedElement method = methods.iterator().next();
                if (!(method instanceof Method)) {
                    return;
                }

                Parameter[] params = ((Method) method).getParameters();
                PsiElement[] args = reference.getParameters();

                inspectArguments(args, params);
            }

            public void visitPhpFunctionCall(FunctionReference reference) {
                PhpIndex objIndex = PhpIndex.getInstance(holder.getProject());

                Collection<Function> functions = objIndex.getFunctionsByName(reference.getName());
                if (functions.size() != 1) {
                    return;
                }

                Parameter[] params = functions.iterator().next().getParameters();
                PsiElement[] args = reference.getParameters();

                inspectArguments(args, params);
            }

            private void inspectArguments(final PsiElement[] args, final Parameter[] params) {
                final int length = Math.min(args.length, params.length);
                for (int i = 0; i < length; ++i) {
                    PhpExpressionTypes argType = new PhpExpressionTypes(args[i], holder);
                    if (argType.isUnknown() && (args[i] instanceof Variable)) {
                        argType = new PhpExpressionTypes(((Variable) args[i]).getType().toString(), holder);
                    }
                    final PhpExpressionTypes paramType = new PhpExpressionTypes(params[i].getType().toString(), holder);

                    if (!paramType.isMixed()
                            && !argType.equals(paramType)
                            && !argType.instanceOf(paramType)
                            && !(paramType.isFloat() && argType.isInt())) {
                        final String strWarning = strProblemDescriptionArgumentTypeMismatch
                                .replace("%t1%", argType.toString())
                                .replace("%t2%", paramType.toString());
                        holder.registerProblem(args[i], strWarning, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }
        };
    }
}
