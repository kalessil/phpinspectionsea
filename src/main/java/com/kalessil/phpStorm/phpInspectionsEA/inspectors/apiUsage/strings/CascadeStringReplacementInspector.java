package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class CascadeStringReplacementInspector extends BasePhpInspection {
    // Inspection options.
    public boolean USE_SHORT_ARRAYS_SYNTAX = false;

    private static final String messageNesting      = "This str_replace(...) call can be merged with its parent.";
    private static final String messageCascading    = "This str_replace(...) call can be merged with the previous.";
    private static final String messageReplacements = "Can be replaced with the string from the array.";

    @NotNull
    public String getShortName() {
        return "CascadeStringReplacementInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpReturn(@NotNull PhpReturn returnStatement) {
                final FunctionReference functionCall = this.getFunctionReference(returnStatement);
                if (functionCall != null) {
                    this.analyze(functionCall, returnStatement);
                }
            }

            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignmentExpression) {
                final FunctionReference functionCall = this.getFunctionReference(assignmentExpression);
                if (functionCall != null) {
                    this.analyze(functionCall, assignmentExpression);
                }
            }

            private void analyze(@NotNull FunctionReference functionCall, @NotNull PsiElement expression) {
                final PsiElement[] arguments = functionCall.getParameters();
                if (arguments.length == 3) {
                    /* case: cascading replacements */
                    final AssignmentExpression previous  = this.getPreviousAssignment(expression);
                    final FunctionReference previousCall = previous == null ? null : this.getFunctionReference(previous);
                    if (previousCall != null) {
                        final PsiElement transitionVariable = previous.getVariable();
                        if (transitionVariable instanceof Variable && arguments[2] instanceof Variable) {
                            final Variable callSubject         = (Variable) arguments[2];
                            final Variable previousVariable    = (Variable) transitionVariable;
                            final PsiElement callResultStorage =
                                    expression instanceof AssignmentExpression ?
                                            ((AssignmentExpression) expression).getVariable() : callSubject;
                            if (
                                callResultStorage != null && callSubject.getName().equals(previousVariable.getName()) &&
                                OpenapiEquivalenceUtil.areEqual(transitionVariable, callResultStorage)
                            ) {
                                holder.registerProblem(
                                    functionCall,
                                    messageCascading,
                                    new MergeStringReplaceCallsFix(functionCall, previousCall, USE_SHORT_ARRAYS_SYNTAX)
                                );
                            }
                        }
                    }

                    /* case: nested replacements */
                    this.checkNestedCalls(arguments[2], functionCall);

                    /* case: search/replace simplification */
                    final PsiElement replace = arguments[1];
                    if (replace instanceof ArrayCreationExpression) {
                        this.checkForSimplification((ArrayCreationExpression) replace);
                    } else if (replace instanceof StringLiteralExpression){
                        final PsiElement search = arguments[0];
                        if (search instanceof ArrayCreationExpression) {
                            this.checkForSimplification((ArrayCreationExpression) search);
                        }
                    }
                }
            }

            private void checkForSimplification(@NotNull ArrayCreationExpression candidate) {
                final Set<String> replacements = new HashSet<>();
                for (final PsiElement oneReplacement : candidate.getChildren()) {
                    if (oneReplacement instanceof PhpPsiElement) {
                        final PhpPsiElement item = ((PhpPsiElement) oneReplacement).getFirstPsiChild();
                        if (!(item instanceof StringLiteralExpression)) {
                            replacements.clear();
                            return;
                        }
                        replacements.add(item.getText());
                    }
                }
                if (replacements.size() == 1) {
                    holder.registerProblem(
                        candidate,
                        messageReplacements,
                        ProblemHighlightType.WEAK_WARNING,
                        new SimplifyReplacementFix(replacements.iterator().next())
                    );
                }
                replacements.clear();
            }

            private void checkNestedCalls(@NotNull PsiElement callCandidate, @NotNull FunctionReference parentCall) {
                if (OpenapiTypesUtil.isFunctionReference(callCandidate)) {
                    final FunctionReference call = (FunctionReference) callCandidate;
                    final String functionName    = call.getName();
                    if (functionName != null && functionName.equals("str_replace")) {
                        holder.registerProblem(
                            callCandidate,
                            messageNesting,
                            new MergeStringReplaceCallsFix(parentCall, call, USE_SHORT_ARRAYS_SYNTAX)
                        );
                    }
                }
            }

            @Nullable
            private FunctionReference getFunctionReference(@NotNull AssignmentExpression assignment) {
                FunctionReference result = null;
                final PsiElement value = ExpressionSemanticUtil.getExpressionTroughParenthesis(assignment.getValue());
                if (OpenapiTypesUtil.isFunctionReference(value)) {
                    final String functionName = ((FunctionReference) value).getName();
                    if (functionName != null && functionName.equals("str_replace")) {
                        result = (FunctionReference) value;
                    }
                }
                return result;
            }

            @Nullable
            private FunctionReference getFunctionReference(@NotNull PhpReturn phpReturn) {
                FunctionReference result = null;
                final PsiElement value
                    = ExpressionSemanticUtil.getExpressionTroughParenthesis(ExpressionSemanticUtil.getReturnValue(phpReturn));
                if (OpenapiTypesUtil.isFunctionReference(value)) {
                    final String functionName = ((FunctionReference) value).getName();
                    if (functionName != null && functionName.equals("str_replace")) {
                        result = (FunctionReference) value;
                    }
                }
                return result;
            }

            @Nullable
            private AssignmentExpression getPreviousAssignment(@NotNull PsiElement returnOrAssignment) {
                /* get previous non-comment, non-php-doc expression */
                PsiElement previous = null;
                if (returnOrAssignment instanceof PhpReturn) {
                    previous = ((PhpReturn) returnOrAssignment).getPrevPsiSibling();
                } else if (returnOrAssignment instanceof AssignmentExpression) {
                    previous = returnOrAssignment.getParent().getPrevSibling();
                }
                while (previous != null && !(previous instanceof PhpPsiElement)) {
                    previous = previous.getPrevSibling();
                }
                while (previous instanceof PhpDocComment) {
                    previous = ((PhpDocComment) previous).getPrevPsiSibling();
                }
                /* grab the target assignment */
                final AssignmentExpression result;
                if (previous != null && previous.getFirstChild() instanceof AssignmentExpression) {
                    result = (AssignmentExpression) previous.getFirstChild();
                } else {
                    result = null;
                }
                return result;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) ->
            component.addCheckbox("Use short arrays syntax", USE_SHORT_ARRAYS_SYNTAX, (isSelected) -> USE_SHORT_ARRAYS_SYNTAX = isSelected)
        );
    }

    private static final class SimplifyReplacementFix extends UseSuggestedReplacementFixer {
        private static final String title = "Simplify this argument";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        SimplifyReplacementFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static final class MergeStringReplaceCallsFix implements LocalQuickFix {
        private static final String title = "Merge str_replace(...) calls";

        final private SmartPsiElementPointer<FunctionReference> patch;
        final private SmartPsiElementPointer<FunctionReference> eliminate;
        final private boolean useShortSyntax;

        MergeStringReplaceCallsFix(@NotNull FunctionReference patch, @NotNull FunctionReference eliminate, boolean useShortSyntax) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(patch.getProject());

            this.patch          = factory.createSmartPsiElementPointer(patch);
            this.eliminate      = factory.createSmartPsiElementPointer(eliminate);
            this.useShortSyntax = useShortSyntax;
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
            final FunctionReference patch     = this.patch.getElement();
            final FunctionReference eliminate = this.eliminate.getElement();
            if (patch != null && eliminate != null) {
                synchronized (eliminate.getContainingFile()) {
                    this.mergeReplaces(patch, eliminate, this.useShortSyntax);
                    this.mergeArguments(patch.getParameters()[0], eliminate.getParameters()[0], this.useShortSyntax);
                    this.mergeSources(patch, eliminate);
                }
            }
        }

        private void mergeArguments(@NotNull PsiElement to, @NotNull PsiElement from, boolean useShortSyntax) {
            final Project project  = to.getProject();
            if (to instanceof ArrayCreationExpression) {
                final PsiElement comma      = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, ",");
                final PsiElement firstValue = ((ArrayCreationExpression) to).getFirstPsiChild();
                final PsiElement marker     = firstValue == null ? null : firstValue.getPrevSibling();
                if (comma != null && marker != null) {
                    if (from instanceof ArrayCreationExpression) {
                        final PsiElement[] values = from.getChildren();
                        ArrayUtils.reverse(values);
                        Arrays.stream(values).forEach(value -> {
                            to.addAfter(comma, marker);
                            to.addAfter(value.copy(), marker);
                        });
                    } else {
                        to.addAfter(comma, marker);
                        to.addAfter(from.copy(), marker);
                    }
                }
            } else {
                if (from instanceof ArrayCreationExpression) {
                    final PsiElement comma       = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, ",");
                    final String pattern         = (useShortSyntax ? "[%1%]" : "array(%1%)").replace("%1%", to.getText());
                    final PsiElement replacement = PhpPsiElementFactory.createPhpPsiFromText(project, ArrayCreationExpression.class, pattern);
                    final PsiElement firstValue  = ((ArrayCreationExpression) replacement).getFirstPsiChild();
                    final PsiElement marker      = firstValue == null ? null : firstValue.getPrevSibling();
                    if (comma != null && marker != null) {
                        final PsiElement[] values = from.getChildren();
                        ArrayUtils.reverse(values);
                        Arrays.stream(values).forEach(value -> {
                            replacement.addAfter(comma, marker);
                            replacement.addAfter(value.copy(), marker);
                        });
                        to.replace(replacement);
                    }
                } else {
                    final String pattern = (useShortSyntax ? "[%1%, %2%]" : "array(%1%, %2%)")
                            .replace("%2%", to.getText())
                            .replace("%1%", from.getText());
                    to.replace(PhpPsiElementFactory.createPhpPsiFromText(project, ArrayCreationExpression.class, pattern));
                }
            }
        }

        @NotNull
        private PsiElement unbox(@NotNull PsiElement what) {
            if (what instanceof ArrayCreationExpression) {
                final PsiElement[] elements = what.getChildren();
                if (elements.length == 1 && !(elements[0] instanceof ArrayHashElement)) {
                    final PsiElement value = elements[0].getFirstChild();
                    if (value instanceof StringLiteralExpression) {
                        what = value;
                    }
                }
            }
            return what;
        }

        private void mergeReplaces(@NotNull FunctionReference to, @NotNull FunctionReference from, boolean useShortSyntax) {
            /* normalization here */
            final PsiElement fromNormalized = this.unbox(from.getParameters()[1]);
            final PsiElement toRaw          = to.getParameters()[1];
            final PsiElement toNormalized   = this.unbox(toRaw);

            /* a little bit of intelligence */
            boolean needsFurtherFixing = true;
            if (toNormalized instanceof StringLiteralExpression) {
                if (
                    fromNormalized instanceof StringLiteralExpression &&
                    fromNormalized.getText().equals(toNormalized.getText())
                ) {
                    toRaw.replace(toNormalized);
                    needsFurtherFixing = false;
                }
            }

            if (needsFurtherFixing) {
                /* in order to perform the proper merging we'll need to expand short-hand replacement definitions */
                this.expandReplacement(to, useShortSyntax);
                this.expandReplacement(from, useShortSyntax);
                this.mergeArguments(to.getParameters()[1], from.getParameters()[1], useShortSyntax);
            }
        }

        private void expandReplacement(@NotNull FunctionReference call, boolean useShortSyntax) {
            final PsiElement[] arguments = call.getParameters();
            final PsiElement search      = arguments[0];
            final PsiElement replace     = arguments[1];
            if (replace instanceof StringLiteralExpression && search instanceof ArrayCreationExpression) {
                final int searchesCount = search.getChildren().length;
                if (searchesCount > 1) {
                    final List<String> replaces = Collections.nCopies(searchesCount, replace.getText());
                    replace.replace(
                        PhpPsiElementFactory.createPhpPsiFromText(
                            call.getProject(),
                            ArrayCreationExpression.class,
                            (useShortSyntax ? "[%a%]" : "array(%a%)").replace("%a%", String.join(", ", replaces))
                        )
                    );
                }
            }
        }

        private void mergeSources(@NotNull FunctionReference patch, @NotNull FunctionReference eliminate) {
            final PsiElement eliminateParent = eliminate.getParent().getParent();
            patch.getParameters()[2].replace(eliminate.getParameters()[2]);
            if (OpenapiTypesUtil.isStatementImpl(eliminateParent)) {
                final PsiElement trailingSpaceCandidate = eliminateParent.getNextSibling();
                if (trailingSpaceCandidate instanceof PsiWhiteSpace) {
                    trailingSpaceCandidate.delete();
                }
                eliminateParent.delete();
            }
        }
    }
}
