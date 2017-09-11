package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.ConcatenationExpression;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class NonSecureCryptUsageInspector extends BasePhpInspection {
    private static final String messageWeakSalt     = "A weak hash generated, consider providing '$2y$<cost and salt>' (Blowfish) as the second argument.";
    private static final String messageInsecureSalt = "'$2y$<cost and salt>' should be used in preference to insecure '$2a$<cost and salt>'.";
    private static final String messagePasswordHash = "Use of password_hash(..., PASSWORD_BCRYPT) is encouraged in this case (uses $2y$ with cost of 10).";

    @NotNull
    public String getShortName() {
        return "NonSecureCryptUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                /* general structure requirements */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if ((1 != params.length && 2 != params.length) || StringUtils.isEmpty(functionName) || !functionName.equals("crypt")) {
                    return;
                }
                /* avoid complaining to imported functions */
                final PsiElement function = reference.resolve();
                if (function instanceof Function && !((Function) function).getFQN().equals("\\crypt")) {
                    return;
                }

                /* Case 1: suggest providing blowfish as the 2nd parameter*/
                if (1 == params.length) {
                    holder.registerProblem(reference, messageWeakSalt);
                    return;
                }

                /* try resolving 2nd parameter, skip if failed, it contains injections or length is not as expected */
                final String saltValue = this.resolveSalt(params[1]);
                if (null == saltValue || saltValue.length() < 4) {
                    return;
                }

                /* Case 2: using $2a$; use $2y$ instead - http://php.net/security/crypt_blowfish.php*/
                if (saltValue.startsWith("$2a$")) {
                    holder.registerProblem(reference, messageInsecureSalt, ProblemHighlightType.GENERIC_ERROR);
                    return;
                }

                /* Case 3: -> password_hash(PASSWORD_BCRYPT) in PHP 5.5+ */
                final boolean isBlowfish = saltValue.startsWith("$2y$") || saltValue.startsWith("$2x$");
                if (isBlowfish) {
                    PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                    if (php.compareTo(PhpLanguageLevel.PHP550) >= 0) {
                        holder.registerProblem(reference, messagePasswordHash, ProblemHighlightType.WEAK_WARNING);
                    }
                }
            }

            @Nullable
            private String resolveSalt(@NotNull PsiElement expression) {
                /* collect possible value for further analysis */
                final Set<PsiElement> discovered = PossibleValuesDiscoveryUtil.discover(expression);
                if (discovered.size() != 1) {
                    discovered.clear();
                    return null;
                }

                /* simplify workflow by handling one expression */
                final PsiElement saltExpression       = discovered.iterator().next();
                final StringBuilder resolvedSaltValue = new StringBuilder();
                discovered.clear();

                /*  resolve string literals and concatenations */
                PsiElement current = saltExpression;
                while (current instanceof ConcatenationExpression) {
                    final ConcatenationExpression concat = (ConcatenationExpression) current;
                    final PsiElement right               = ExpressionSemanticUtil.getExpressionTroughParenthesis(concat.getRightOperand());

                    StringLiteralExpression part = ExpressionSemanticUtil.resolveAsStringLiteral(right);
                    resolvedSaltValue.insert(0, null == part ? "<?>" : part.getContents());

                    current = ExpressionSemanticUtil.getExpressionTroughParenthesis(concat.getLeftOperand());
                }

                /* don't forget to add the last element */
                if (null != current) {
                    final StringLiteralExpression lastPart = ExpressionSemanticUtil.resolveAsStringLiteral(current);
                    resolvedSaltValue.insert(0, null == lastPart ? "<?>" : lastPart.getContents());
                }

                return resolvedSaltValue.toString();
            }
        };
    }

}
