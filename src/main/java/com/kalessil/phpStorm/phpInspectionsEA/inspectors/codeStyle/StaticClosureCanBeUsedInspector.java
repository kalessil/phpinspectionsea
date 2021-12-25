package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class StaticClosureCanBeUsedInspector extends BasePhpInspection {
    private static final String message = "This closure can be declared as static (better scoping; in some cases can improve performance).";

    // Inspection options.
    public boolean SUGGEST_FOR_SHORT_FUNCTIONS = true;

    @NotNull
    @Override
    public String getShortName() {
        return "StaticClosureCanBeUsedInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Static closure can be used";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunction(@NotNull Function function) {
                if (PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP540) && OpenapiTypesUtil.isLambda(function)) {
                    final boolean isTarget = ! OpenapiTypesUtil.is(function.getFirstChild(), PhpTokenTypes.kwSTATIC);
                    if (isTarget && this.canBeStatic(function)) {
                        holder.registerProblem(
                                function.getFirstChild(),
                                MessagesPresentationUtil.prefixWithEa(message),
                                new MakeClosureStaticFix()
                        );
                    }
                    /* learning currently is not feasible: influence of co-dispatched arguments */
                }
            }

            private boolean canBeStatic(@NotNull Function function) {
                final boolean isArrowFunction = ! OpenapiTypesUtil.is(function.getFirstChild(), PhpTokenTypes.kwFUNCTION);
                final PsiElement body         = isArrowFunction ? function : ExpressionSemanticUtil.getGroupStatement(function);
                if (body != null) {
                    final boolean isTargetClosure = (isArrowFunction && SUGGEST_FOR_SHORT_FUNCTIONS) ||
                                                    ExpressionSemanticUtil.countExpressionsInGroup((GroupStatement) body) > 0;
                    if (isTargetClosure) {
                        /* check if $this or parent:: being used */
                        for (final PsiElement element : PsiTreeUtil.findChildrenOfAnyType(body, Variable.class, MethodReference.class)) {
                            if (element instanceof Variable) {
                                if (((Variable) element).getName().equals("this")) {
                                    return false;
                                }
                            } else {
                                final MethodReference reference = (MethodReference) element;
                                final PsiElement base           = reference.getFirstChild();
                                if (base instanceof ClassReference && base.getText().equals("parent")) {
                                    final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                                    if (resolved instanceof Method && ! ((Method) resolved).isStatic()) {
                                        return false;
                                    }
                                }
                            }
                        }
                        /* check usages: perhaps bound to closure or returned -> then we can not promote */
                        /* Closure::bind(, null, ) -> can be static */
                        if (! this.canBeStatic(this.usages(function))) {
                            return false;
                        }

                        return true;
                    }
                }
                return false;
            }

            private List<PsiElement> usages(@NotNull Function function) {
                final List<PsiElement> usages    = new ArrayList<>();

                /* case 1: dispatched directly into a call */
                final PsiElement parent          = function.getParent();
                final PsiElement extractedParent = OpenapiTypesUtil.is(parent, PhpElementTypes.CLOSURE) ? parent.getParent() : parent;
                if (extractedParent instanceof ParameterList) {
                    usages.add(extractedParent);
                    return usages;
                }

                /* case 2: dispatched into a call via variable */
                if (OpenapiTypesUtil.isAssignment(extractedParent)) {
                    final PsiElement assignmentStorage = ((AssignmentExpression) extractedParent).getVariable();
                    if (assignmentStorage instanceof Variable) {
                        final Function scope           = ExpressionSemanticUtil.getScope(function);
                        final GroupStatement scopeBody = scope == null ? null : ExpressionSemanticUtil.getGroupStatement(scope);
                        if (scopeBody != null) {
                            final String variableName = ((Variable) assignmentStorage).getName();
                            for (final Variable usage : PsiTreeUtil.findChildrenOfType(scopeBody, Variable.class)) {
                                if (variableName.equals(usage.getName()) && usage != assignmentStorage) {
                                    final PsiElement usageContext = usage.getParent();
                                    /* if closure used in other context, side-effects are not predictable */
                                    if (! (usageContext instanceof ParameterList) && ! (usageContext instanceof MethodReference)) {
                                        usages.clear();
                                        return usages;
                                    }
                                    usages.add(usageContext);
                                }
                            }
                        }
                    }
                }

                /* case 3: dispatched into array (factories/callbacks) in non-scoped context */
                if (OpenapiTypesUtil.is(extractedParent, PhpElementTypes.ARRAY_VALUE)) {
                    usages.add(extractedParent.getParent());
                }

                return usages;
            }

            private boolean canBeStatic(@NotNull List<PsiElement> usages) {
                for (final PsiElement context : usages) {
                    if (context instanceof ParameterList) {
                        final PsiElement referenceCandidate = context.getParent();
                        if (referenceCandidate instanceof MethodReference) {
                            /* case: Closure::bind() */
                            final MethodReference reference = (MethodReference) referenceCandidate;
                            final String methodName         = reference.getName();
                            if (methodName != null && methodName.equals("bind")) {
                                final PsiElement[] arguments = reference.getParameters();
                                if (arguments.length > 1 && PhpLanguageUtil.isNull(arguments[1])) {
                                    final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                                    if (resolved instanceof Method && ((Method) resolved).getFQN().equals("\\Closure.bind")) {
                                        continue;
                                    }
                                }
                            } else {
                                final PsiElement operator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
                                if (OpenapiTypesUtil.is(operator, PhpTokenTypes.SCOPE_RESOLUTION)) {
                                    continue;
                                }
                            }
                        } else if (referenceCandidate instanceof FunctionReference) {
                            /* case: e.g. array_filter($array, $callback) */
                            final PsiElement resolved = OpenapiResolveUtil.resolveReference((FunctionReference) referenceCandidate);
                            if (resolved instanceof Function && ((Function) resolved).getNamespaceName().equals("\\")) {
                                continue;
                            }
                        }
                    } else if (context instanceof MethodReference) {
                        /* case: $closure->bindTo() */
                        final MethodReference reference = (MethodReference) context;
                        final String methodName         = reference.getName();
                        if (methodName != null && methodName.equals("bindTo")) {
                            final PsiElement[] arguments = reference.getParameters();
                            if (arguments.length > 0 && PhpLanguageUtil.isNull(arguments[0])) {
                                final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                                if (resolved instanceof Method && ((Method) resolved).getFQN().equals("\\Closure.bindTo")) {
                                    continue;
                                }
                            }
                        }
                    } else if (context instanceof ArrayHashElement) {
                        /* case: [ Clazz::class => <callback> ] (factories) */
                        if (ExpressionSemanticUtil.getScope(context) == null) {
                            continue;
                        }
                    }
                    return false;
                }
                return true;
            }
        };
    }

    private static final class MakeClosureStaticFix implements LocalQuickFix {
        private static final String title = "Declare the closure static";

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
            final PsiElement functionKeyword = descriptor.getPsiElement();
            if (functionKeyword != null && ! project.isDisposed()) {
                final PsiElement implant = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, "static");
                if (implant != null) {
                    functionKeyword.getParent().addBefore(implant, functionKeyword);
                }
            }
        }
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
                component.addCheckbox("Suggest for short functions", SUGGEST_FOR_SHORT_FUNCTIONS, (isSelected) -> SUGGEST_FOR_SHORT_FUNCTIONS = isSelected)
        );
    }
}
