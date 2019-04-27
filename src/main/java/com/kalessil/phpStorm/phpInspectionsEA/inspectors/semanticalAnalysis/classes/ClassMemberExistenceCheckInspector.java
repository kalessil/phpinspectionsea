package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ClassMemberExistenceCheckInspector extends BasePhpInspection {
    private static final String messageTrue  = "This call seems to always return true, please check the workflow.";
    private static final String messageFalse = "This call seems to always return false, perhaps a wrong function being used.";

    final private static Set<String> targetFunctions = new HashSet<>();
    static {
        targetFunctions.add("method_exists");
        targetFunctions.add("property_exists");
    }

    @NotNull
    public String getShortName() {
        return "ClassMemberExistenceCheckInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && targetFunctions.contains(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length >= 2) {
                        final String memberName = this.extractMemberName(arguments[1]);
                        if (!memberName.isEmpty()) {
                            final PhpClass clazz = this.extractClass(arguments[0]);
                            if (clazz != null) {
                                /* resolve the member */
                                PhpClassMember member = null;
                                if (functionName.equals("method_exists")) {
                                    member = OpenapiResolveUtil.resolveMethod(clazz, memberName);
                                    if (member == null && OpenapiResolveUtil.resolveField(clazz, memberName) != null) {
                                        holder.registerProblem(reference, messageFalse);
                                    }
                                } else if (functionName.equals("property_exists")) {
                                    member = OpenapiResolveUtil.resolveField(clazz, memberName);
                                    if (member == null && OpenapiResolveUtil.resolveMethod(clazz, memberName) != null) {
                                        holder.registerProblem(reference, messageFalse);
                                    }
                                }
                                /* analyze */
                                if (member != null && ExpressionSemanticUtil.getBlockScope(member) instanceof PhpClass) {
                                    holder.registerProblem(reference, messageTrue);
                                }
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
