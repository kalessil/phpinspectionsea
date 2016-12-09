package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class ReferencingObjectsInspector extends BasePhpInspection {
    private static final String messageParameter  = "Objects are always passed by reference; please correct '& $%p%'";
    private static final String messageAssignment = "Objects are always passed by reference; please correct '= & new '";

    private static final PhpType php7Types = new PhpType();
    static {
        php7Types.add(PhpType.STRING);
        php7Types.add(PhpType.INT);
        php7Types.add(PhpType.FLOAT);
        php7Types.add(PhpType.BOOLEAN);
        php7Types.add(PhpType.ARRAY);
    }

    @NotNull
    public String getShortName() {
        return "ReferencingObjectsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            /* re-dispatch to inspector */
            public void visitPhpMethod(Method method) {
                this.inspectCallable(method);
            }
            public void visitPhpFunction(Function function) {
                this.inspectCallable(function);
            }

            private void inspectCallable (@NotNull Function callable) {
                if (null == callable.getNameIdentifier()) {
                    return;
                }

                for (Parameter parameter : callable.getParameters()) {
                    if (
                        parameter.isPassByRef() && !parameter.getDeclaredType().isEmpty() &&
                        !PhpType.isSubType(parameter.getDeclaredType(), php7Types)
                    ) {
                        final String message = messageParameter.replace("%p%", parameter.getName());
                        holder.registerProblem(parameter, message, ProblemHighlightType.WEAK_WARNING, new ParameterLocalFix(parameter));
                    }
                }
            }

            public void visitPhpNewExpression(NewExpression expression) {
                final PsiElement parent = expression.getParent();
                if (parent instanceof AssignmentExpression) {
                    final AssignmentExpression assignment = (AssignmentExpression) parent;
                    if (assignment.getValue() == expression) {
                        PsiElement operation = assignment.getValue().getPrevSibling();
                        if (operation instanceof PsiWhiteSpace) {
                            operation = operation.getPrevSibling();
                        }

                        if (null != operation && operation.getText().replaceAll("\\s+","").equals("=&")) {
                            holder.registerProblem(expression, messageAssignment, ProblemHighlightType.WEAK_WARNING, new InstantiationLocalFix(operation));
                        }
                    }
                }
            }
        };
    }

    private static class InstantiationLocalFix implements LocalQuickFix {
        private PsiElement assignOperator;

        InstantiationLocalFix(@NotNull PsiElement assignOperator) {
            super();
            this.assignOperator = assignOperator;
        }

        @NotNull
        @Override
        public String getName() {
            return "Replace with regular assignment";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            LeafPsiElement replacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "=");
            //noinspection ConstantConditions - expression is hardcoded so we safe from NPE here
            this.assignOperator.replace(replacement);

            /* release a tree node reference */
            this.assignOperator = null;
        }
    }

    private static class ParameterLocalFix implements LocalQuickFix {
        private Parameter parameter;

        ParameterLocalFix(@NotNull Parameter parameter) {
            super();
            this.parameter = parameter;
        }

        @NotNull
        @Override
        public String getName() {
            return "Cleanup parameter definition";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement nameNode =  this.parameter.getNameIdentifier();
            if (null != nameNode) {
                PsiElement previous = nameNode.getPrevSibling();
                if (previous instanceof PsiWhiteSpace) {
                    previous = previous.getPrevSibling();
                    previous.getNextSibling().delete();
                }

                previous.delete();
            }

            /* release a tree node reference */
            this.parameter = null;
        }
    }
}
