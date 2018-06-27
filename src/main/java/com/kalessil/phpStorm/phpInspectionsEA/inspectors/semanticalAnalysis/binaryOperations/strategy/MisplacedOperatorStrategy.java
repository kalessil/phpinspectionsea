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
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

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
            final PsiElement[] arguments = call.getParameters();
            final PsiElement candidate   = arguments.length > 0 ? arguments[arguments.length - 1] : null;
            /* BO should be the last parameter of a call in logical contexts */
            if (candidate == expression && ExpressionSemanticUtil.isUsedAsLogicalOperand(parent)) {
                final PsiElement resolved = OpenapiResolveUtil.resolveReference(call);
                if (resolved instanceof Function) {
                    /* resolve the last parameter types: if bool not defined implicitly, continue */
                    final Function function  = (Function) resolved;
                    final Parameter[] params = function.getParameters();
                    if (params.length >= arguments.length) {
                        final Parameter parameter = params[arguments.length - 1];
                        final Set<String> types   = new HashSet<>();
                        parameter.getType().filterUnknown().getTypes().forEach(t -> types.add(Types.getType(t)));
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
        }

        return false;
    }

    private static final class MisplacedOperatorFix implements LocalQuickFix {
        private static final String title = "Place the operator correctly";

        final private String expression;
        final private SmartPsiElementPointer<PsiElement> call;

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        MisplacedOperatorFix(@NotNull String expression, @NotNull PsiElement call) {
            super();

            this.expression = expression;
            this.call       = SmartPointerManager.getInstance(call.getProject()).createSmartPsiElementPointer(call);
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = call.getElement();
            if (expression != null) {
                final String pattern = '(' + this.expression + ')';
                final ParenthesizedExpression replacement
                        = PhpPsiElementFactory.createPhpPsiFromText(project, ParenthesizedExpression.class, pattern);
                expression.replace(replacement.getArgument());
            }
        }
    }
}
