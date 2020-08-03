package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Include;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class UntrustedInclusionInspector extends BasePhpInspection {
    private static final String message = "This relies on include_path and not guaranteed to load the right file. Concatenate with __DIR__ or use namespaces + class loading instead.";

    final static private Pattern absolutePathPattern;
    static {
        /* original regex: ^(/|[a-z]:).*$ */
        absolutePathPattern = Pattern.compile("^(/|[a-z]:).*$", Pattern.CASE_INSENSITIVE);
    }

    @NotNull
    @Override
    public String getShortName() {
        return "UntrustedInclusionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Untrusted files inclusion";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpInclude(@NotNull Include include) {
                final StringLiteralExpression file = ExpressionSemanticUtil.resolveAsStringLiteral(include.getArgument());
                if (file != null) {
                    final String path = file.getContents();
                    if (!path.isEmpty() && !absolutePathPattern.matcher(path).matches()) {
                        holder.registerProblem(
                                include,
                                MessagesPresentationUtil.prefixWithEa(message)
                        );
                    }
                }
            }
        };
    }
}