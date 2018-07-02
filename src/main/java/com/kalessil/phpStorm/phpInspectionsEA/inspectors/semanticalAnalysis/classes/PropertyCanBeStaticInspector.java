package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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
    public String getShortName() {
        return "PropertyCanBeStaticInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (holder.getFile() != clazz.getContainingFile()) { return; }

                final PhpLanguageLevel php    = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                final boolean canUseConstants = php.compareTo(PhpLanguageLevel.PHP560) >= 0;

                /* parent class might already introduce fields */
                final PhpClass parent = OpenapiResolveUtil.resolveSuperClass(clazz);
                for (final Field field : clazz.getOwnFields()) {
                    /* if we can report field at all */
                    final PsiElement nameNode = NamedElementUtil.getNameIdentifier(field);
                    if (nameNode == null) {
                        continue;
                    }
                    /* skip static, public properties and all constants - they should not be changed via constructor */
                    final PhpModifier modifier  = field.getModifier();
                    final boolean isTargetField = !field.isConstant() && !modifier.isStatic() && !modifier.isPublic();
                    if (!isTargetField || !(field.getDefaultValue() instanceof ArrayCreationExpression)) {
                        continue;
                    }
                    /* do not process overriding, there is an inspection for this */
                    if (parent != null && OpenapiResolveUtil.resolveField(parent, field.getName()) != null) {
                        continue;
                    }

                    /* look into array for key-value pairs */
                    /* TODO: merge into next loop */
                    int intArrayOrStringCount = 0;
                    for (final ArrayHashElement entry : ((ArrayCreationExpression) field.getDefaultValue()).getHashElements()) {
                        final PhpPsiElement item = entry.getValue();
                        if (item instanceof ArrayCreationExpression || item instanceof StringLiteralExpression) {
                            ++intArrayOrStringCount;
                            if (intArrayOrStringCount == 3) {
                                holder.registerProblem(nameNode, canUseConstants ? messageWithConstants : messageNoConstants);
                                break;
                            }
                        }
                    }
                    /* look into array for value only pairs */
                    if (intArrayOrStringCount < 3) {
                        for (final PsiElement entry : field.getDefaultValue().getChildren()) {
                            if (entry instanceof PhpPsiElement && !(entry instanceof ArrayHashElement)) {
                                final PhpPsiElement item = ((PhpPsiElement) entry).getFirstPsiChild();
                                if (item instanceof ArrayCreationExpression || item instanceof StringLiteralExpression) {
                                    ++intArrayOrStringCount;
                                    if (intArrayOrStringCount == 3) {
                                        holder.registerProblem(nameNode, canUseConstants ? messageWithConstants : messageNoConstants);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
