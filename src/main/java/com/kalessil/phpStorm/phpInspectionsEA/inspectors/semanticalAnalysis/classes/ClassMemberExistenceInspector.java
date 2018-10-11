package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.EAUltimateApplicationComponent;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

final public class ClassMemberExistenceInspector extends BasePhpInspection {

    final private static Set<String> targetFunctions = new HashSet<>();
    static {
        targetFunctions.add("method_exists");
        targetFunctions.add("property_exists");
    }

    @NotNull
    public String getShortName() {
        return "ClassExistenceCheckInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (!EAUltimateApplicationComponent.areFeaturesEnabled()) { return; }
                if (this.isContainingFileSkipped(reference))              { return; }

                final String functionName = reference.getName();
                if (functionName != null && targetFunctions.contains(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 2) {
                        final String memberName = this.extractMemberName(arguments[1]);
                        if (!memberName.isEmpty()) {
                            final PhpClass clazz = this.extractClass(arguments[1]);
                            if (clazz != null) {
                                /* resolve the member */
                                PhpClassMember member = null;
                                if (functionName.equals("method_exists")) {
                                    member = OpenapiResolveUtil.resolveMethod(clazz, memberName);
                                } else if (functionName.equals("property_exists")) {
                                    member = OpenapiResolveUtil.resolveField(clazz, memberName);
                                }
                                /* analyze */
                            }
                        }
                    }
                }
            }

            @Nullable
            private PhpClass extractClass(@NotNull PsiElement expression) {
                PhpClass result = null;
                if (expression instanceof PhpTypedElement) {
                    final Project project = expression.getProject();
                    final PhpType type    = OpenapiResolveUtil.resolveType((PhpTypedElement) expression, project);
                    if (type != null && !type.hasUnknown()) {
                        final Set<String> resolved = type.filterNull().getTypes().stream()
                                .map(Types::getType)
                                .collect(Collectors.toSet());
                        if (resolved.size() == 1) {
                            final String fqn                   = resolved.iterator().next();
                            final PhpIndex index               = PhpIndex.getInstance(project);
                            final Collection<PhpClass> classes = OpenapiResolveUtil.resolveClassesByFQN(fqn, index);
                            if (classes.size() == 1) {
                                result = classes.iterator().next();
                            }
                        }
                        resolved.clear();
                    }
                }
                return result;
            }

            @NotNull
            private String extractMemberName(@NotNull PsiElement expression) {
                String result = "";
                final StringLiteralExpression member = ExpressionSemanticUtil.resolveAsStringLiteral(expression);
                if (member != null && member.getFirstPsiChild() == null) {
                    result = member.getContents();
                }
                return result;
            }
        };
    }
}
