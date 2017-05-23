package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.lowerAccessLevel.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

                    /* find fields references matching pre-collected names */
                    for (final FieldReference reference :PsiTreeUtil.findChildrenOfType(body, FieldReference.class)) {
                        final String referenceName = reference.getName();
                        if (!fields.containsKey(referenceName)) {
                            continue;
                        }

                        /* store the context information */
                        final PsiElement resolved = reference.resolve();
                        if (resolved != null && fields.get(referenceName) == resolved) {
                            if (!contextInformation.containsKey(referenceName)) {
                                contextInformation.put(referenceName, new HashSet<>());
                            }
                            final Set<String> usages        = contextInformation.get(referenceName);
                            final PhpModifier.Access access = method.getAccess();
                            if (access.isPrivate()) {
                                usages.add("private");
                            }
                            if (access.isProtected()) {
                                usages.add("protected");
                            }
                            if (access.isPublic()) {
                                usages.add("public");
                            }
                        }
                    }
                }

                for (final String fieldName : fields.keySet()) {
                    if (!contextInformation.containsKey(fieldName)) {
                        continue;
                    }
                    final Set<String> usages = contextInformation.get(fieldName);
                    if (usages.size() == 1 && usages.contains("private")) {
                        final PsiElement modifier = getProtectedModifier(fields.get(fieldName));
                        if (modifier != null) {
                            holder.registerProblem(modifier, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new TheLocalFix(modifier));
                        }
                    }
                }
                contextInformation.clear();
                fields.clear();
            }
        }
    }

    @Nullable
    private static PsiElement getProtectedModifier(final PhpClassMember subject) {
        final PsiElement expression      = (subject instanceof Field) ? subject.getParent() : subject;
        final PhpModifierList list       = PsiTreeUtil.findChildOfType(expression, PhpModifierList.class);
        final LeafPsiElement[] modifiers = PsiTreeUtil.getChildrenOfType(list, LeafPsiElement.class);
        PsiElement result                = null;
        if (modifiers != null) {
            for (final LeafPsiElement modifier : modifiers) {
                if (modifier.getText().equalsIgnoreCase("protected")) {
                    result = modifier;
                    break;
                }
            }
        }
        return result;
    }

    private static class TheLocalFix implements LocalQuickFix {
        private final SmartPsiElementPointer<PsiElement> modifier;

        TheLocalFix(@NotNull final PsiElement modifierElement) {
            final SmartPointerManager manager = SmartPointerManager.getInstance(modifierElement.getProject());

            modifier = manager.createSmartPsiElementPointer(modifierElement);
        }

        @NotNull
        @Override
        public String getName() {
            return "Make it private";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final PsiElement element     = modifier.getElement();
            final PsiElement replacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "private");
            if (element != null && replacement != null) {
                element.replace(replacement);
            }
        }
    }
}
