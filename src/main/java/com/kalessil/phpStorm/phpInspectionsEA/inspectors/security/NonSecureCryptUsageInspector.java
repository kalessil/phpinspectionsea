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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

public class NonSecureCryptUsageInspector extends BasePhpInspection {
    private static final String messageWeakSalt     = "A weak hash generated, consider providing '$2y$' (Blowfish) as the second argument.";
    private static final String messageInsecureSalt = "'$2y$' should be used in preference to insecure '$2a$'.";
    private static final String messagePasswordHash = "Use of password_hash(..., PASSWORD_BCRYPT) is encouraged in this case (uses $2y$ salt).";

    @NotNull
    public String getShortName() {
        return "NonSecureCryptUsageInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                /* general structure requirements */
                final String functionName = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if ((1 != params.length && 2 != params.length) || StringUtil.isEmpty(functionName) || !functionName.equals("crypt")) {
                    return;
                }
                /* avoid complaining to imported functions */
                final PsiElement function = reference.resolve();
                if (function instanceof Function && !((Function) function).getFQN().equals("\\crypt")) {
                    return;
                }

                /* Case 1: suggest providing blowfish as the 2nd parameter*/
                if (1 == params.length) {
                    holder.registerProblem(reference, messageWeakSalt, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    return;
                }

                /* try resolving 2nd parameter, skip if failed, it contains injections or length is not as expected */
                final StringLiteralExpression salt = ExpressionSemanticUtil.resolveAsStringLiteral(params[1]);
                final String saltValue             = null == salt ? null : salt.getContents();
                if (null == saltValue || 4 != saltValue.length() || null != salt.getFirstPsiChild()) {
                    return;
                }

                /* Case 2: using $2a$; use $2y$ instead - http://php.net/security/crypt_blowfish.php*/
                if (saltValue.equals("$2a$")) {
                    holder.registerProblem(reference, messageInsecureSalt, ProblemHighlightType.GENERIC_ERROR);
                    return;
                }

                /* Case 3: -> password_hash(PASSWORD_BCRYPT) in PHP 5.5+ */
                final boolean isBlowfish = saltValue.equals("$2y$") || saltValue.equals("$2x$");
                if (isBlowfish) {
                    PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                    if (php.compareTo(PhpLanguageLevel.PHP550) >= 0) {
                        // if salt is $2y$, provide a quick-fix
                        holder.registerProblem(reference, messagePasswordHash, ProblemHighlightType.GENERIC_ERROR);
                    }
                }
            }
        };
    }

}
