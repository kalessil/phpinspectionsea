package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UselessReturnInspector extends BasePhpInspection {
    private static final String messageSenseless = "Senseless statement: return null implicitly or safely remove it.";
    private static final String messageConfusing = "Assignment here is not making much sense.";

    @NotNull
    public String getShortName() {
        return "UselessReturnInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpReturn(@NotNull PhpReturn expression) {
                final PhpExpression value = ExpressionSemanticUtil.getReturnValue(expression);
                if (value instanceof AssignmentExpression) {
                    final AssignmentExpression assignment = (AssignmentExpression) value;
                    final PsiElement container            = assignment.getVariable();
                    if (container instanceof Variable) {
                        final Function scope = ExpressionSemanticUtil.getScope(expression);
                        if (scope != null) {
                            final Variable variable = (Variable) container;
                            if (!this.isArgumentReference(variable, scope) && !this.isBoundReference(variable, scope)) {
                                holder.registerProblem(expression, messageConfusing);
                            }
                        }
                    }
                }
            }

            @Override
            public void visitPhpMethod(@NotNull Method method) {
                this.inspectForSenselessReturn(method);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                this.inspectForSenselessReturn(function);
            }

            private void inspectForSenselessReturn(@NotNull Function callable) {
                final GroupStatement body      = ExpressionSemanticUtil.getGroupStatement(callable);
                final PsiElement lastStatement = body == null ? null : ExpressionSemanticUtil.getLastStatement(body);
                if (lastStatement instanceof PhpReturn) {
                    final PhpExpression returnValue = ExpressionSemanticUtil.getReturnValue((PhpReturn) lastStatement);
                    if (returnValue == null) {
                        holder.registerProblem(lastStatement, messageSenseless, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                    }
                }
            }

            private boolean isArgumentReference(@NotNull Variable variable, @NotNull Function function) {
                boolean result            = false;
                final String variableName = variable.getName();
                for (final Parameter parameter : function.getParameters()) {
                    if (parameter.getName().equals(variableName) && parameter.isPassByRef()) {
                        result = true;
                        break;
                    }
                }
                return result;

            }

            private boolean isBoundReference(@NotNull Variable variable, @NotNull Function function) {
                boolean result            = false;
                final List<Variable> used = ExpressionSemanticUtil.getUseListVariables(function);
                if (used != null) {
                    final String variableName      = variable.getName();
                    final Optional<Variable> match = used.stream().filter(v -> v.getName().equals(variableName)).findFirst();
                    if (match.isPresent()) {
                        final PsiElement previous  = match.get().getPrevSibling();
                        final PsiElement candidate = previous instanceof PsiWhiteSpace ? previous.getPrevSibling() : previous;
                        result                     = OpenapiTypesUtil.is(candidate, PhpTokenTypes.opBIT_AND);
                    }
                    used.clear();
                }
                return result;
            }
        };
    }
}

