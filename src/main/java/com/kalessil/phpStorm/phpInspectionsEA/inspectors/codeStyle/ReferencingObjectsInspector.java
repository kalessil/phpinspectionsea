package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.GenericPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiPsiSearchUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ReferencingObjectsInspector extends PhpInspection {
    private static final String messageParameter  = "Objects are always passed by reference; please correct '& $%s'.";
    private static final String messageAssignment = "Objects are always passed by reference; please correct '= & new '.";

    private static final PhpType php7Types = new PhpType();
    static {
        /* implicit scalar types */
        php7Types.add(PhpType.STRING);
        php7Types.add(PhpType.INT);
        php7Types.add(PhpType.FLOAT);
        php7Types.add(PhpType.BOOLEAN);
        php7Types.add(PhpType.ARRAY);
        /* nullability support */
        php7Types.add(PhpType.NULL);
    }

    @NotNull
    public String getShortName() {
        return "ReferencingObjectsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GenericPhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.shouldSkipAnalysis(method, StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE)) { return; }

                this.inspectCallable(method);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (this.shouldSkipAnalysis(function, StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE)) { return; }

                this.inspectCallable(function);
            }

            private void inspectCallable (@NotNull Function callable) {
                if (NamedElementUtil.getNameIdentifier(callable) != null) {
                    Arrays.stream(callable.getParameters())
                        .filter(parameter -> {
                            if (parameter.isPassByRef()) {
                                final PhpType declared = parameter.getDeclaredType();
                                return !declared.isEmpty() && !PhpType.isSubType(declared, php7Types);
                            }
                            return false;
                        })
                        .filter(parameter -> {
                            final String parameterName = parameter.getName();
                            final GroupStatement body  = ExpressionSemanticUtil.getGroupStatement(callable);
                            for (final Variable variable : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
                                if (parameterName.equals(variable.getName())) {
                                    final PsiElement parent = variable.getParent();
                                    if (parent instanceof AssignmentExpression) {
                                        final AssignmentExpression assignment = (AssignmentExpression) parent;
                                        if (assignment.getValue() != variable) {
                                            return false;
                                        }
                                    } else if (ExpressionSemanticUtil.isUsedAsLogicalOperand(variable)) {
                                        return false;
                                    }
                                }
                            }
                            return true;
                        })
                        .forEach(parameter ->
                                holder.registerProblem(
                                        parameter,
                                        String.format(messageParameter, parameter.getName()),
                                        new ParameterLocalFix(holder.getProject(), parameter)
                                )
                        );
                }
            }

            @Override
            public void visitPhpNewExpression(@NotNull NewExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE)) { return; }

                final PsiElement parent = expression.getParent();
                if (parent instanceof AssignmentExpression) {
                    final AssignmentExpression assignment = (AssignmentExpression) parent;
                    if (OpenapiTypesUtil.isAssignmentByReference(assignment)) {
                        holder.registerProblem(
                                expression,
                                messageAssignment,
                                new InstantiationLocalFix()
                        );
                    }
                }
            }
        };
    }

    private static final class InstantiationLocalFix implements LocalQuickFix {
        private static final String title = "Replace with regular assignment";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement target = descriptor.getPsiElement().getParent();
            if (target instanceof AssignmentExpression && !project.isDisposed()) {
                final PsiElement operator = OpenapiPsiSearchUtil.findAssignmentOperator((AssignmentExpression) target);
                if (operator != null) {
                    final PsiElement replacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "=");
                    if (replacement != null) {
                        operator.replace(replacement);
                    }
                }
            }
        }
    }

    private static final class ParameterLocalFix implements LocalQuickFix {
        private static final String title = "Cleanup parameter definition";

        final private SmartPsiElementPointer<Parameter> parameter;

        ParameterLocalFix(@NotNull Project project, @NotNull Parameter parameter) {
            super();

            this.parameter = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(parameter);
        }

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement nameNode = NamedElementUtil.getNameIdentifier(this.parameter.getElement());
            if (nameNode != null && !project.isDisposed()) {
                PsiElement previous = nameNode.getPrevSibling();
                if (previous instanceof PsiWhiteSpace) {
                    previous = previous.getPrevSibling();
                    previous.getNextSibling().delete();
                }
                previous.delete();
            }
        }
    }
}
