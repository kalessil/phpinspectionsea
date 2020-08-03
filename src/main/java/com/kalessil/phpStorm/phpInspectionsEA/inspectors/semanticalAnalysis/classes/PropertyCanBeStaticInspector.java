package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class PropertyCanBeStaticInspector extends BasePhpInspection {
    private static final String messageNoConstants   = "This property initialization seems to be quite 'heavy', consider using static property instead.";
    private static final String messageWithConstants = "This property initialization seems to be quite 'heavy', consider using static property or constant instead.";

    @NotNull
    @Override
    public String getShortName() {
        return "PropertyCanBeStaticInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Property could be static";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpField(@NotNull Field field) {
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(field);
                if (nameNode != null) {
                    final PhpModifier modifier = field.getModifier();
                    if (!field.isConstant() && !modifier.isStatic() && !modifier.isPublic()) {
                        final PsiElement defaultValue = field.getDefaultValue();
                        if (defaultValue instanceof ArrayCreationExpression) {
                            final PhpClass clazz = field.getContainingClass();
                            if (clazz != null) {
                                final PhpClass parent = OpenapiResolveUtil.resolveSuperClass(clazz);
                                if (parent == null || OpenapiResolveUtil.resolveField(parent, field.getName()) == null) {
                                    int intArrayOrStringCount = 0;
                                    for (final PsiElement entry : defaultValue.getChildren()) {
                                        PhpPsiElement item = null;
                                        if (entry instanceof ArrayHashElement) {
                                            item = ((ArrayHashElement) entry).getValue();
                                        } else if (entry instanceof PhpPsiElement){
                                            item = ((PhpPsiElement) entry).getFirstPsiChild();
                                        }
                                        if (item instanceof ArrayCreationExpression || item instanceof StringLiteralExpression) {
                                            if (++intArrayOrStringCount == 3 && !this.isSuppressed(field)) {
                                                final boolean canUseConstants = PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP560);
                                                holder.registerProblem(
                                                        nameNode,
                                                        MessagesPresentationUtil.prefixWithEa(canUseConstants ? messageWithConstants : messageNoConstants)
                                                );
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private boolean isSuppressed(@NotNull PsiElement expression) {
                final PsiElement parent = expression.getParent();
                if (parent.getParent() instanceof PhpClass) {
                    final PsiElement previous = ((PhpPsiElement) parent).getPrevPsiSibling();
                    if (previous instanceof PhpDocComment) {
                        final String candidate = previous.getText();
                        if (candidate.contains("@noinspection") && candidate.contains(getShortName())) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }
}
