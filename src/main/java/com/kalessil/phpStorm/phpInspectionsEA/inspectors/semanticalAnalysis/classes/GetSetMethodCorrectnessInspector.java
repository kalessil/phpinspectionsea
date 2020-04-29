package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class GetSetMethodCorrectnessInspector extends PhpInspection {
    private static final String messagePattern = "It's probably a wrong field was used here ('%s' could fit).";

    private static final Pattern regexTargetName;
    static {
        regexTargetName = Pattern.compile("^(set|get|is)([A-Z][a-z]+)+$");
    }

    @NotNull
    @Override
    public String getShortName() {
        return "GetSetMethodCorrectnessInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "get/set methods correctness";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.shouldSkipAnalysis(method, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String methodName = method.getName();
                if (!methodName.isEmpty() && regexTargetName.matcher(methodName).matches()) {
                    final boolean isTargetMethod = !method.isAbstract() &&
                                                   method.getAccess().isPublic() &&
                                                   method.getParameters().length < 2;
                    if (isTargetMethod && !this.isTestContext(method)) {
                        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                        if (body != null && ExpressionSemanticUtil.countExpressionsInGroup(body) > 0) {
                            final Set<String> usedFields = new HashSet<>();
                            for (final FieldReference reference : PsiTreeUtil.findChildrenOfType(body, FieldReference.class)) {
                                final PsiElement base = reference.getFirstChild();
                                if (base instanceof Variable && ((Variable) base).getName().equals("this")) {
                                    usedFields.add(reference.getName());
                                }
                            }
                            if (!usedFields.isEmpty()) {
                                final String methodNameNormalized = methodName.replaceFirst("^(set|get|is)", "").replaceAll("_", "").toLowerCase();
                                final boolean usesTheRightField   = usedFields.stream().anyMatch(fieldName -> this.normalize(fieldName).equals(methodNameNormalized));
                                if (!usesTheRightField) {
                                    final PhpClass clazz = method.getContainingClass();
                                    if (clazz != null) {
                                        final List<String> alternatives = new ArrayList<>();
                                        final boolean hasAlternatives   = clazz.getFields().stream().anyMatch(field -> {
                                            if (!field.isConstant()) {
                                                final String normalized = this.normalize(field.getName());
                                                if (normalized.equals(methodNameNormalized)) {
                                                    final PsiElement scope = ExpressionSemanticUtil.getBlockScope(field);
                                                    return scope instanceof PhpClass && alternatives.add(field.getName());
                                                }
                                            }
                                            return false;
                                        });
                                        if (hasAlternatives) {
                                            final boolean isDelegating = PsiTreeUtil.findChildrenOfType(body, MethodReference.class).stream()
                                                    .anyMatch(reference -> methodName.equals(reference.getName()));
                                            if (!isDelegating) {
                                                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(method);
                                                if (nameNode != null) {
                                                    holder.registerProblem(
                                                            nameNode,
                                                            String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), alternatives.get(0))
                                                    );
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            usedFields.clear();
                        }
                    }
                }
            }

            @NotNull
            private String normalize(@NotNull String fieldName) {
                return fieldName.replaceFirst("^(is)", "").replaceAll("_", "").toLowerCase();
            }
        };
    }
}
