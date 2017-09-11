package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MisplacedOperatorStrategy {
    private static final String message = "This operator is probably misplaced.";

    private static final Set<IElementType> operations = new HashSet<>();
    static {
        operations.add(PhpTokenTypes.opEQUAL);
        operations.add(PhpTokenTypes.opNOT_EQUAL);
        operations.add(PhpTokenTypes.opIDENTICAL);
        operations.add(PhpTokenTypes.opNOT_IDENTICAL);
        operations.add(PhpTokenTypes.opGREATER);
        operations.add(PhpTokenTypes.opGREATER_OR_EQUAL);
        operations.add(PhpTokenTypes.opLESS);
        operations.add(PhpTokenTypes.opLESS_OR_EQUAL);
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement parent = expression.getParent().getParent();
        /* basic operator filter */
        if (parent instanceof FunctionReference && operations.contains(expression.getOperationType())) {
            final FunctionReference call = (FunctionReference) parent;
            final PsiElement[] params    = call.getParameters();
            final PsiElement candidate   = params.length > 0 ? params[params.length - 1] : null;
            /* BO should be the last parameter of a call in logical contexts */
            if (candidate == expression && ExpressionSemanticUtil.isUsedAsLogicalOperand(parent)) {
                final PsiElement resolved = call.resolve();
                if (resolved instanceof Function) {
                    /* resolve the last parameter types: if bool not defined implicitly, continue */
                    final Function function   = (Function) resolved;
                    final Parameter parameter = function.getParameters()[params.length - 1];
                    final Set<String> types   =
                            parameter.getType().filterUnknown().getTypes().stream()
                                    .map(Types::getType)
                                    .collect(Collectors.toSet());
                    if (!types.contains(Types.strBoolean)) {
                        final PsiElement rightOperand = expression.getRightOperand();
                        final PsiElement leftOperand  = expression.getLeftOperand();
                        if (leftOperand != null && rightOperand instanceof PhpTypedElement) {
                            final Project project      = holder.getProject();
                            final PhpType allowedTypes = function.getType().global(project).filterUnknown();
                            final PhpType operandTypes = ((PhpTypedElement) rightOperand).getType().global(project).filterUnknown();
                            final PsiElement operator  = expression.getOperation();
                            if (operator != null && allowedTypes.getTypes().containsAll(operandTypes.getTypes())) {
                                final String replacement = "%c% %o% %r%"
                                        .replace("%r%", rightOperand.getText())
                                        .replace("%o%", operator.getText())
                                        .replace("%c%", call.getText())
                                        .replace(expression.getText(), leftOperand.getText());
                                holder.registerProblem(operator, message, new MisplacedOperatorFix(replacement, call));
                                return true;
                            }
                        }
                    }
                    types.clear();
                }
            }
        }

        return false;
    }

    private static class MisplacedOperatorFix implements LocalQuickFix {
        final private String expression;
        final private SmartPsiElementPointer<PsiElement> call;

        @NotNull
        @Override
        public String getName() {
            return "Place the operator correctly";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        MisplacedOperatorFix(@NotNull String expression, @NotNull PsiElement call) {
            this.expression = expression;
            SmartPointerManager manager = SmartPointerManager.getInstance(call.getProject());

            this.call = manager.createSmartPsiElementPointer(call);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = call.getElement();
            if (expression != null) {
                final String pattern = "(" + this.expression + ")";
                final ParenthesizedExpression replacement
                        = PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, pattern);
                expression.replace(replacement.getArgument());
            }
        }
    }
}
