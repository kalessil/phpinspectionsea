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
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
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
    private static final String messageParameter  = "Objects are always passed by reference; please correct '& $%p%'.";
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
        return new BasePhpElementVisitor() {
            /* re-dispatch to inspector */
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                this.inspectCallable(method);
            }

            @Override
            public void visitPhpFunction(@NotNull Function function) {
                this.inspectCallable(function);
            }

            private void inspectCallable (@NotNull Function callable) {
                if (NamedElementUtil.getNameIdentifier(callable) != null) {
                    Arrays.stream(callable.getParameters())
                        .filter(parameter -> {
                            final PhpType declared = parameter.getDeclaredType();
                            return !declared.isEmpty() && parameter.isPassByRef() && !PhpType.isSubType(declared, php7Types);
                        })
                        .filter(parameter -> {
                            boolean result             = true;
                            final String parameterName = parameter.getName();
                            final GroupStatement body  = ExpressionSemanticUtil.getGroupStatement(callable);
                            for (final Variable variable : PsiTreeUtil.findChildrenOfType(body, Variable.class)) {
                                if (parameterName.equals(variable.getName())) {
                                    final PsiElement parent = variable.getParent();
                                    if (parent instanceof AssignmentExpression) {
                                        final boolean isWrite = ((AssignmentExpression) parent).getVariable() == variable;
                                        if (isWrite) {
                                            result = false;
                                            break;
                                        }
                                    } else if (ExpressionSemanticUtil.isUsedAsLogicalOperand(variable)) {
                                        result = false;
                                        break;
                                    }
                                }
                            }
                            return result;
                        })
                        .forEach(parameter -> {
                            final String message = messageParameter.replace("%p%", parameter.getName());
                            holder.registerProblem(parameter, message, new ParameterLocalFix(parameter));
                        });
                }
            }

            @Override
            public void visitPhpNewExpression(@NotNull NewExpression expression) {
                final PsiElement parent = expression.getParent();
                if (parent instanceof AssignmentExpression) {
                    final AssignmentExpression assignment = (AssignmentExpression) parent;
                    if (assignment.getValue() == expression) {
                        PsiElement operation = assignment.getValue().getPrevSibling();
                        if (operation instanceof PsiWhiteSpace) {
                            operation = operation.getPrevSibling();
                        }

                        if (operation != null && operation.getText().replaceAll("\\s+", "").equals("=&")) {
                            holder.registerProblem(expression, messageAssignment, new InstantiationLocalFix(operation));
                        }
                    }
                }
            }
        };
    }

    private static final class InstantiationLocalFix implements LocalQuickFix {
        private static final String title = "Replace with regular assignment";

        final private SmartPsiElementPointer<PsiElement> assignOperator;

        InstantiationLocalFix(@NotNull PsiElement assignOperator) {
            super();

            this.assignOperator = SmartPointerManager.getInstance(assignOperator.getProject()).createSmartPsiElementPointer(assignOperator);
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
            final PsiElement assignOperator = this.assignOperator.getElement();
            if (assignOperator != null && !project.isDisposed()) {
                final PsiElement replacement = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "=");
                if (replacement != null) {
                    assignOperator.replace(replacement);
                }
            }
        }
    }

    private static final class ParameterLocalFix implements LocalQuickFix {
        private static final String title = "Cleanup parameter definition";

        final private SmartPsiElementPointer<Parameter> parameter;

        ParameterLocalFix(@NotNull Parameter parameter) {
            super();

            this.parameter = SmartPointerManager.getInstance(parameter.getProject()).createSmartPsiElementPointer(parameter);
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
