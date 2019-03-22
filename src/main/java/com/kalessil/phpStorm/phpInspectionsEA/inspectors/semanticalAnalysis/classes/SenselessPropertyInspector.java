package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SenselessPropertyInspector extends BasePhpInspection {
    private static final String messagePattern = "'%s' property seems to be used as a local variable in '%s' method.";

    @NotNull
    public String getShortName() {
        return "SenselessPropertyInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpClass(@NotNull PhpClass clazz) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(clazz))                  { return; }

                if (!clazz.isInterface() && !clazz.isTrait()) {
                    final List<Field> fields = Stream.of(clazz.getOwnFields())
                            .filter(f -> !f.isConstant() && f.getModifier().isPrivate() && !f.getModifier().isStatic())
                            .filter(f -> {
                                final PhpDocTag[] tags  = PsiTreeUtil.getChildrenOfType(f.getDocComment(), PhpDocTag.class);
                                final boolean annotated = tags != null && Arrays.stream(tags).anyMatch(t -> !t.getName().equals(t.getName().toLowerCase()));
                                return !annotated;
                            })
                            .collect(Collectors.toList());
                    if (!fields.isEmpty()) {
                        final Map<String, Set<String>> fieldsUsages = this.extractFieldsUsage(clazz);
                        if (!fieldsUsages.isEmpty()) {
                            fields.forEach(f -> {
                                final String fieldName = f.getName();
                                if (fieldsUsages.containsKey(fieldName) && fieldsUsages.get(fieldName).size() == 1) {
                                    final String methodName = fieldsUsages.get(fieldName).iterator().next();
                                    /* false-positives: unused constructor dependency */
                                    if (!methodName.equals("__construct")) {
                                        final Method method = OpenapiResolveUtil.resolveMethod(clazz, methodName);
                                        if (method != null && this.isTarget(f, method)) {
                                            holder.registerProblem(f, String.format(messagePattern, fieldName, methodName));
                                        }
                                    }
                                }
                            });
                            fieldsUsages.values().forEach(Set::clear);
                            fieldsUsages.clear();
                        }
                        fields.clear();
                    }
                }
            }

            private boolean isTarget(@NotNull Field field, @NotNull Method method) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                if (body != null) {
                    final String fieldName               = field.getName();
                    final Optional<FieldReference> first = PsiTreeUtil.findChildrenOfType(body, FieldReference.class).stream()
                            .filter(r -> {
                                if (fieldName.equals(r.getName())) {
                                    final PsiElement base = r.getFirstChild();
                                    return base instanceof Variable && ((Variable) base).getName().equals("this");
                                }
                                return false;
                            })
                            .findFirst();
                    if (first.isPresent()) {
                        final FieldReference reference = first.get();
                        final PsiElement parent        = reference.getParent();
                        if (OpenapiTypesUtil.isAssignment(parent)) {
                            final AssignmentExpression assignment = (AssignmentExpression) parent;
                            return assignment.getValue() != reference;
                        }
                    }
                }
                return false;
            }

            private Map<String, Set<String>> extractFieldsUsage(@NotNull PhpClass clazz) {
                final Map<String, Set<String>> result = new HashMap<>();
                Stream.of(clazz.getOwnMethods())
                        .filter(m -> !m.isStatic() && !m.isAbstract())
                        .forEach(method -> {
                            final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                            for (final FieldReference reference : PsiTreeUtil.findChildrenOfType(body, FieldReference.class)) {
                                final PsiElement base = reference.getFirstChild();
                                if (base instanceof Variable && ((Variable) base).getName().equals("this")) {
                                    final String fieldName = reference.getName();
                                    result.putIfAbsent(fieldName, new HashSet<>());
                                    result.get(fieldName).add(method.getName());
                                }
                            }
                        });
                return result;
            }
        };
    }
}
