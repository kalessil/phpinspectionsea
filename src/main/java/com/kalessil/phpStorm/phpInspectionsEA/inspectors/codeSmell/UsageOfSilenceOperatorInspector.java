package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Funivan <alotofall@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UsageOfSilenceOperatorInspector extends BasePhpInspection {
    // Inspection options.
    public boolean optionRespectContext = false;

    private static final String message = "Try to avoid using the @, as it hides problems and complicates troubleshooting.";

    private static final List<String> suppressibleFunctions = Arrays.asList(
            "\\fclose",            // if fails to close a file, app will have more serious problems ^_^
            "\\closedir",          // quite the same as above
            "\\ftp_close",         // quite the same as above
            "\\ldap_unbind",       // quite the same as above
            "\\ldap_free_result",  // quite the same as above
            "\\sqlite_close",      // quite the same as above
            "\\mysql_close",       // quite the same as above
            "\\mysqli_close",      // quite the same as above
            "\\pg_close",          // quite the same as above

            "\\filesize",          // expect people to be smart enough and check the existence before the call
            "\\filemtime",         // same as above
            "\\unlink",            // same as above
            "\\rmdir",             // same as above
            "\\chmod",             // same as above
            "\\ftp_chmod",         // same as above

            "\\file_exists",       // honest checks
            "\\posix_isatty",      // honest checks
            "\\class_exists",      // honest checks
            "\\get_resource_type", // honest checks
            "\\getenv",            // honest checks

            "\\trigger_error"      // own error handler will take care about emitted issues
    );

    @NotNull
    public String getShortName() {
        return "UsageOfSilenceOperatorInspection";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            public void visitPhpUnaryExpression(UnaryExpression unaryExpression) {
                /* general structure expectations */
                final PsiElement suppressionCandidate = unaryExpression.getOperation();
                if (null == suppressionCandidate || PhpTokenTypes.opSILENCE != suppressionCandidate.getNode().getElementType()) {
                    return;
                }

                /* valid contexts: `... = @...`, ` return @... ` */
                PsiElement parent = unaryExpression.getParent();
                if (optionRespectContext) {
                    if (parent instanceof TernaryExpression) {
                        parent = parent.getParent();
                    }
                    if (parent instanceof AssignmentExpression || parent instanceof PhpReturn) {
                        return;
                    }
                }

                /* pattern 1: whatever but not a function call */
                final PsiElement suppressedExpression = unaryExpression.getValue();
                if (!OpenapiTypesUtil.isFunctionReference(suppressedExpression)) {
                    if (null != suppressedExpression) {
                        holder.registerProblem(suppressionCandidate, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix());
                    }

                    return;
                }

                /* pattern 2: a function call */
                final PsiElement resolved = ((FunctionReference) suppressedExpression).resolve();
                final String functionName = resolved instanceof Function ? ((Function) resolved).getFQN() : null;
                if (null == functionName || suppressibleFunctions.contains(functionName)) {
                    return;
                }

                if (optionRespectContext) {
                    /* valid context: ` false === @... `, ` false !== @... ` */
                    if (parent instanceof BinaryExpression) {
                        final BinaryExpression parentExpression = (BinaryExpression) parent;
                        final IElementType operator             = parentExpression.getOperationType();
                        if (PhpTokenTypes.opIDENTICAL == operator || PhpTokenTypes.opNOT_IDENTICAL == operator) {
                            PsiElement falseCandidate = parentExpression.getLeftOperand();
                            if (falseCandidate == unaryExpression) {
                                falseCandidate = parentExpression.getRightOperand();
                            }
                            if (PhpLanguageUtil.isFalse(falseCandidate)) {
                                return;
                            }
                        }
                    }
                    /* valid context: logical true/false contexts */
                    if (ExpressionSemanticUtil.isUsedAsLogicalOperand(unaryExpression)) {
                        return;
                    }
                }

                holder.registerProblem(suppressionCandidate, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix());
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.createCheckbox("Content aware reporting", optionRespectContext, (isSelected) -> optionRespectContext = isSelected);
        });
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Remove @";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement operator = descriptor.getPsiElement();
            if (null != operator && operator.getParent() instanceof UnaryExpression) {
                final UnaryExpression suppression = (UnaryExpression) operator.getParent();
                //noinspection ConstantConditions as structure guaranted
                suppression.replace(suppression.getValue().copy());
            }
        }
    }
}
