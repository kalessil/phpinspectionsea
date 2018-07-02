package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CryptographicallySecureRandomnessInspector extends BasePhpInspection {
    private static final String messageUseRandomBytes                = "Consider using cryptographically secure random_bytes() instead.";
    private static final String messageVerifyBytes                   = "The IV generated can be false, please add necessary checks.";
    private static final String messageOpenssl2ndArgumentNotDefined  = "Use 2nd parameter for determining if the algorithm used was cryptographically strong.";
    private static final String messageMcrypt2ndArgumentNotDefined   = "Please provide 2nd parameter implicitly as default value has changed between PHP versions.";

    private static final String messageOpenssl2ndArgumentNotVerified = "$crypto_strong can be false, please add necessary checks.";
    private static final String messageMcrypt2ndArgumentNotSecure    = "It's better to use MCRYPT_DEV_RANDOM here (may block until more entropy is available).";

    @NotNull
    public String getShortName() {
        return "CryptographicallySecureRandomnessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName == null || (!functionName.equals("openssl_random_pseudo_bytes") && !functionName.equals("mcrypt_create_iv"))) {
                    return;
                }
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length != 1 && arguments.length != 2) {
                    return;
                }
                final boolean isOpenSSL = functionName.equals("openssl_random_pseudo_bytes");


                /* Case 1: use random_bytes in PHP7 */
                PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP7 and newer
                    holder.registerProblem(reference, messageUseRandomBytes, ProblemHighlightType.WEAK_WARNING);
                }


                /* Case 2: report missing 2nd argument */
                final boolean hasSecondArgument = arguments.length == 2;
                if (!hasSecondArgument) {
                    final String message = isOpenSSL ? messageOpenssl2ndArgumentNotDefined : messageMcrypt2ndArgumentNotDefined;
                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                }


                /* Case 3: unchecked generation result */
                /* TODO: get parent thru parentheses and silence operator - util method */
                /* unwrap reference if it silenced */
                PsiElement parent = reference.getParent();
                if (parent instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) parent;
                    if (OpenapiTypesUtil.is(unary.getOperation(), PhpTokenTypes.opSILENCE)) {
                        parent = parent.getParent();
                    }
                }
                /* check if result has been saved and verified against false */
                boolean resultVerified = false;
                if (parent instanceof AssignmentExpression) {
                    final PsiElement variable = ((AssignmentExpression) parent).getVariable();
                    resultVerified            = null == variable || isCheckedForFalse(variable);
                }
                if (!resultVerified) {
                    holder.registerProblem(reference, messageVerifyBytes, ProblemHighlightType.GENERIC_ERROR);
                }

                /* Case 4: is 2nd argument verified/strong enough */
                if (hasSecondArgument && !isOpenSSL) {
                    boolean reliableSource = true; /* we'll check expected constant below */
                    if (arguments[1] instanceof ConstantReference) {
                        final ConstantReference secondArgument = (ConstantReference) arguments[1];
                        final String constant                  = secondArgument.getName();
                        reliableSource = constant != null && constant.equals("MCRYPT_DEV_RANDOM");
                    }
                    if (!reliableSource) {
                        holder.registerProblem(arguments[1], messageMcrypt2ndArgumentNotSecure, ProblemHighlightType.GENERIC_ERROR);
                        return;
                    }
                }
                if (hasSecondArgument && isOpenSSL) {
                    if (!isCheckedForFalse(arguments[1]) && arguments[1].getTextLength() > 0) {
                        holder.registerProblem(arguments[1], messageOpenssl2ndArgumentNotVerified, ProblemHighlightType.GENERIC_ERROR);
                    }
                }
            }

            private boolean isCheckedForFalse(@NotNull PsiElement subject) {
                /* ensure we are in a callable, and assume checked if it's plain script (no false-positives) */
                final Function scope      = ExpressionSemanticUtil.getScope(subject);
                final GroupStatement body = null == scope ? null : ExpressionSemanticUtil.getGroupStatement(scope);
                if (null == body) {
                    return true;
                }

                boolean isChecked = false;
                for (BinaryExpression expression : PsiTreeUtil.findChildrenOfType(body, BinaryExpression.class)) {
                    final PsiElement left       = expression.getLeftOperand();
                    final PsiElement right      = expression.getRightOperand();
                    final IElementType operator = expression.getOperationType();
                    if (operator != null && left != null && right != null) {
                        /* expression should be a comparison with a false */
                        if (!PhpLanguageUtil.isFalse(left) && !PhpLanguageUtil.isFalse(right)) {
                            continue;
                        }
                        if (PhpTokenTypes.opIDENTICAL != operator && PhpTokenTypes.opNOT_IDENTICAL != operator) {
                            continue;
                        }

                        final PsiElement operatorValue = PhpLanguageUtil.isFalse(left) ? right : left;
                        if (OpenapiEquivalenceUtil.areEqual(operatorValue, subject)) {
                            isChecked = true;
                            break;
                        }
                    }
                }

                return isChecked;
            }
        };
    }
}
