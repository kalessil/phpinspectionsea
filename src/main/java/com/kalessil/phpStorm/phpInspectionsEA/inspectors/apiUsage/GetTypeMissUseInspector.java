package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.UnaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiElementsUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class GetTypeMissUseInspector extends BasePhpInspection {
    private static final String messagePattern     = "'%i%%f%(%p%)' construction is more compact and easier to read.";
    private static final String messageInvalidType = "'%t%' is not a value returned by 'gettype(...)'.";

    @NotNull
    public String getShortName() {
        return "GetTypeMissUseInspection";
    }

    private static final Map<String, String> mapping = new HashMap<>();
    static {
        mapping.put("boolean",  "is_bool");
        mapping.put("integer",  "is_int");
        mapping.put("double",   "is_float");
        mapping.put("string",   "is_string");
        mapping.put("array",    "is_array");
        mapping.put("object",   "is_object");
        mapping.put("resource", "is_resource");
        mapping.put("NULL",     "is_null");
        // "unknown type" will not be processed: no is_* analog
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName == null || !functionName.equals("gettype")) {
                    return;
                }
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length != 1) {
                    return;
                }

                /* check context: expected to be binary expression */
                final PsiElement parent = reference.getParent();
                if (!(parent instanceof BinaryExpression)) {
                    return;
                }

                /* ensure valid operations being analyzed */
                final BinaryExpression expression = (BinaryExpression) parent;
                final IElementType operator       = expression.getOperationType();
                if (!OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(operator)) {
                    return;
                }

                /* ensure the 2nd operator is string literal and we are taking care about it */
                final PsiElement type                     = OpenapiElementsUtil.getSecondOperand(expression, reference);
                final StringLiteralExpression typeLiteral = ExpressionSemanticUtil.resolveAsStringLiteral(type);
                if (typeLiteral == null) {
                    return;
                }
                final String typeString = typeLiteral.getContents();
                if (!mapping.containsKey(typeString)) {
                    /* edge case: compared string is wrong xD - bug */
                    if (!typeString.equals("unknown type")) {
                        final String message = messageInvalidType.replace("%t%", typeString);
                        holder.registerProblem(type, message, ProblemHighlightType.GENERIC_ERROR);
                    }

                    return;
                }

                /* now we can report */
                final String suggestedName = mapping.get(typeString);
                final boolean isInverted   = PhpTokenTypes.opNOT_EQUAL == operator || PhpTokenTypes.opNOT_IDENTICAL == operator;
                final String message = messagePattern
                        .replace("%p%", arguments[0].getText())
                        .replace("%f%", suggestedName)
                        .replace("%i%", isInverted ? "!" : "");
                holder.registerProblem(parent, message, new TheLocalFix(suggestedName, arguments[0], isInverted));
            }
        };
    }

    private static final class TheLocalFix implements LocalQuickFix {
        private static final String title = "Use suggested is_*(...) function instead";

        final private String suggestedName;
        final private boolean isInverted;
        final private SmartPsiElementPointer<PsiElement> param;

        TheLocalFix(@NotNull String suggestedName, @NotNull PsiElement param, boolean isInverted) {
            super();

            this.suggestedName = suggestedName;
            this.param         = SmartPointerManager.getInstance(param.getProject()).createSmartPsiElementPointer(param);
            this.isInverted    = isInverted;
        }

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

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            final PsiElement param      = this.param.getElement();
            if (param != null && expression instanceof BinaryExpression) {
                final String pattern =
                        (isInverted ? "!" : "") +
                        (suggestedName + "(%p%)".replace("%p%", param.getText()));

                final PsiElement replacement;
                if (isInverted) {
                    replacement = PhpPsiElementFactory.createFromText(project, UnaryExpression.class, pattern);
                } else {
                    replacement = PhpPsiElementFactory.createFromText(project, FunctionReference.class, pattern);
                }

                if (replacement != null) {
                    expression.replace(replacement);
                }
            }
        }
    }
}
