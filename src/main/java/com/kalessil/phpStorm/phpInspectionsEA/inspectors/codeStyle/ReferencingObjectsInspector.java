package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
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

public class ReferencingObjectsInspector extends BasePhpInspection {
    private static final String messageParameter  = "Objects are always passed by reference; please correct '& $%s'.";
    private static final String messageAssignment = "Objects are always passed by reference; please correct '= & new '.";

    private static final PhpType supportedScalarTypes = new PhpType();
    static {
        /* implicit scalar types */
        supportedScalarTypes.add(PhpType.STRING);
        supportedScalarTypes.add(PhpType.INT);
        supportedScalarTypes.add(PhpType.FLOAT);
        supportedScalarTypes.add(PhpType.BOOLEAN);
        supportedScalarTypes.add(PhpType.ARRAY);
        supportedScalarTypes.add(PhpType.MIXED);
        supportedScalarTypes.add(new PhpType().add("\\iterable"));
        /* nullability support */
        supportedScalarTypes.add(PhpType.NULL);
    }

    @NotNull
    @Override
    public String getShortName() {
        return "ReferencingObjectsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Referencing objects";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                this.analyze(method);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                this.analyze(function);
            }

            private void analyze(@NotNull Function callable) {
                if (!OpenapiTypesUtil.isLambda(callable)) {
                    Arrays.stream(callable.getParameters())
                        .filter(parameter -> {
                            if (parameter.isPassByRef() && parameter.getDefaultValue() == null) {
                                final PhpType declared = OpenapiResolveUtil.resolveDeclaredType(parameter);
                                return !declared.isEmpty() && !PhpType.isSubType(declared, supportedScalarTypes);
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
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageParameter), parameter.getName()),
                                        new ParameterLocalFix(holder.getProject(), parameter)
                                )
                        );
                }
            }

            @Override
            public void visitPhpNewExpression(@NotNull NewExpression expression) {
                final PsiElement parent = expression.getParent();
                if (parent instanceof AssignmentExpression && OpenapiTypesUtil.isAssignmentByReference((AssignmentExpression) parent)) {
                    holder.registerProblem(
                            expression,
                            MessagesPresentationUtil.prefixWithEa(messageAssignment),
                            new InstantiationLocalFix()
                    );
                }
            }
        };
    }

    private static final class InstantiationLocalFix implements LocalQuickFix {
        private static final String title = "Replace with regular assignment";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
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
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
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
