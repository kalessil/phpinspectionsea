package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.strategy;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.fixers.MakePrivateFixer;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.utils.ModifierExtractionUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class PropertyUsedInPrivateContextStrategy {
    private static final String message = "Since the property used in private context only, it could be declared private.";

    private static final Set<String> magicMethods = new HashSet<>();
    static {
        // magicMethods.add("__construct"); DI, protected might be needed for extension
        // magicMethods.add("__destruct");
        // magicMethods.add("__autoload");  deprecated
        // magicMethods.add("__call");      calls
        // magicMethods.add("__callStatic");
        // magicMethods.add("__invoke");

        magicMethods.add("__get");
        magicMethods.add("__isset");
        magicMethods.add("__unset");
        magicMethods.add("__set");

        magicMethods.add("__toString");
        magicMethods.add("__debugInfo");

        magicMethods.add("__set_state");
        magicMethods.add("__wakeup");
        magicMethods.add("__sleep");
        magicMethods.add("__clone");
    }

    public static void apply(@NotNull PhpClass clazz, @NotNull ProblemsHolder holder) {
        if (!clazz.isFinal() && !clazz.isInterface()) {
            final PhpClass parent           = clazz.getSuperClass();
            final Map<String, Field> fields = new HashMap<>();
            for (final Field field : clazz.getOwnFields()) {
                if (field.getModifier().isProtected()) {
                    /* ensure overrides are skipped */
                    final String fieldName = field.getName();
                    if (parent == null || parent.findFieldByName(fieldName, false) == null) {
                        fields.put(fieldName, field);
                    }
                }
            }
            if (!fields.isEmpty()) {
                /* collect usage contexts: iterate methods */
                final Map<String, Set<String>> contextInformation = new HashMap<>();
                for (final Method method : clazz.getOwnMethods()) {
                    final GroupStatement body = method.isAbstract() ? null : ExpressionSemanticUtil.getGroupStatement(method);
                    if (body == null) {
                        continue;
                    }
                    final boolean isMagicMethod = magicMethods.contains(method.getName());

                    /* find fields references matching pre-collected names */
                    for (final FieldReference reference :PsiTreeUtil.findChildrenOfType(body, FieldReference.class)) {
                        final String referenceName = reference.getName();
                        if (!fields.containsKey(referenceName)) {
                            continue;
                        }

                        /* store the context information */
                        final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                        if (resolved != null && fields.get(referenceName) == resolved) {
                            if (!contextInformation.containsKey(referenceName)) {
                                contextInformation.put(referenceName, new HashSet<>());
                            }
                            final Set<String> usages        = contextInformation.get(referenceName);
                            final PhpModifier.Access access = method.getAccess();
                            if (isMagicMethod || access.isPrivate()) {
                                usages.add("private");
                            }
                            if (!isMagicMethod && access.isProtected()) {
                                usages.add("protected");
                            }
                            if (!isMagicMethod && access.isPublic()) {
                                usages.add("public");
                            }
                        }
                    }
                }

                fields.forEach((fieldName, list) -> {
                    if (contextInformation.containsKey(fieldName)) {
                        final Set<String> usages = contextInformation.get(fieldName);
                        if (usages.size() == 1 && usages.contains("private")) {
                            final PsiElement modifier = ModifierExtractionUtil.getProtectedModifier(list);
                            if (modifier != null) {
                                holder.registerProblem(modifier, message, new MakePrivateFixer(modifier));
                            }
                        }
                    }
                });
                fields.clear();

                contextInformation.clear();
            }
        }
    }
}
