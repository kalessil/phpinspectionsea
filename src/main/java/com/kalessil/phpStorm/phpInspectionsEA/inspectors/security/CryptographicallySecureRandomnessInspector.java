package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
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
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
/*
 - Cryptographically secure random:
     - openssl_random_pseudo_bytes()
         - report missing 2nd argument;
         - report if argument is not verified;
     - mcrypt_create_iv()
         - report if MCRYPT_DEV_RANDOM (strong) MCRYPT_DEV_RANDOM (secure) not provided as 2nd argument
         - report using MCRYPT_DEV_RANDOM and side-effects;
 */

public class CryptographicallySecureRandomnessInspector extends BasePhpInspection {
    private static final String messageUseRandomBytes                    = "Consider using cryptographically secure random_bytes() instead";
    private static final String messageVerifyBytes                       = "The IV generated can be false, please add necessary checks";
    private static final String messageOpenssl2ndArgumentNotDefined      = "Use 2nd parameter for determining if the algorithm used was cryptographically strong";
    private static final String messageMcrypt2ndArgumentNotDefined       = "Please provide 2nd parameter implicitly as default value has changed between PHP versions";


    private static final String messageOpenssl2ndArgumentNotVerified     = "..."; // error
    private static final String messageMcrypt2ndArgumentStrongNotSecure  = "..."; // error

    @NotNull
    public String getShortName() {
        return "CryptographicallySecureRandomnessInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* genera and function name requirements */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if ((1 != params.length && 2 != params.length) || StringUtil.isEmpty(functionName)) {
                    return;
                }
                if (!functionName.equals("openssl_random_pseudo_bytes") && !functionName.equals("mcrypt_create_iv")) {
                    return;
                }
                final boolean isOpenSSL = functionName.equals("openssl_random_pseudo_bytes");


                /* Case 1: use random_bytes in PHP7 */
                PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                if (php.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) { // PHP7 and newer
                    holder.registerProblem(reference, messageUseRandomBytes, ProblemHighlightType.WEAK_WARNING);
                }


                /* Case 2: report missing 2nd argument */
                final boolean hasSecondArgument = 2 == params.length;
                if (!hasSecondArgument) {
                    final String message = isOpenSSL ? messageOpenssl2ndArgumentNotDefined : messageMcrypt2ndArgumentNotDefined;
                    holder.registerProblem(reference, message, ProblemHighlightType.GENERIC_ERROR);
                }


                /* Case 3: unchecked generation result */
                boolean resultVerified = false;
                if (reference.getParent() instanceof AssignmentExpression) {
                    final AssignmentExpression assignment = (AssignmentExpression) reference.getParent();
                    final PsiElement assignmentContainer  = assignment.getVariable();
                    if (assignment.getValue() == reference && null != assignmentContainer) {
                        final Function scope      = ExpressionSemanticUtil.getScope(reference);
                        final GroupStatement body = null == scope ? null : ExpressionSemanticUtil.getGroupStatement(scope);
                        if (null != body) {
                            Collection<BinaryExpression> checks = PsiTreeUtil.findChildrenOfType(body, BinaryExpression.class);
                            for (BinaryExpression expression : checks) {
                                /* ensure binary expression is complete */
                                final PsiElement left      = expression.getLeftOperand();
                                final PsiElement right     = expression.getRightOperand();
                                final PsiElement operation = expression.getOperation();
                                if (null == operation || null == left || null == right) {
                                    continue;
                                }

                                /* expression should be a comparison with a false */
                                if (!PhpLanguageUtil.isFalse(left) && !PhpLanguageUtil.isFalse(right)) {
                                    continue;
                                }
                                final IElementType operator = operation.getNode().getElementType();
                                if (PhpTokenTypes.opIDENTICAL != operator && PhpTokenTypes.opNOT_IDENTICAL != operator) {
                                    continue;
                                }

                                final PsiElement operatorValue = PhpLanguageUtil.isFalse(left) ? right : left;
                                if (PsiEquivalenceUtil.areElementsEquivalent(operatorValue, assignmentContainer)) {
                                    resultVerified = true;
                                    break;
                                }
                            }
                            checks.clear();
                        }
                    }
                }
                if (!resultVerified) {
                    holder.registerProblem(reference, messageVerifyBytes, ProblemHighlightType.GENERIC_ERROR);
                }

                /* Case 4: is 2nd argument verified/strong enough */
            }
        };
    }
}
