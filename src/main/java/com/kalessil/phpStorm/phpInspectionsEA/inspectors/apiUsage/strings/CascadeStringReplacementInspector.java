package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings;

import com.intellij.codeInsight.PsiEquivalenceUtil;
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
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private static final String messageNesting      = "This str_replace(...) call can be merged with its parent.";
    private static final String messageCascading    = "This str_replace(...) call can be merged with the previous.";
    private static final String messageReplacements = "Can be replaced with the string duplicated in array.";

    @NotNull
    public String getShortName() {
        return "CascadeStringReplacementInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignmentExpression) {
                final FunctionReference functionCall = this.getStrReplaceReference(assignmentExpression);
                if (functionCall != null) {
                    final PsiElement[] params = functionCall.getParameters();
                    if (params.length == 3) {
                        /* case: cascading replacements */
                        final AssignmentExpression previous  = this.getPreviousAssignment(assignmentExpression);
                        final FunctionReference previousCall = previous == null ? null : this.getStrReplaceReference(previous);
                        if (previousCall != null) {
                            final PsiElement transitionVariable = previous.getVariable();
                            if (transitionVariable instanceof Variable && params[2] instanceof Variable) {
                                /* ensure previous, used and result storage is the same variable */
                                final String previousVariableName  = ((Variable) transitionVariable).getName();
                                final String callSubjectName       = ((Variable) params[2]).getName();
                                final PsiElement callResultStorage = assignmentExpression.getVariable();
                                if (
                                    callResultStorage != null && callSubjectName.equals(previousVariableName) &&
                                    PsiEquivalenceUtil.areElementsEquivalent(transitionVariable, callResultStorage)
                                ) {
                                    holder.registerProblem(
                                        functionCall,
                                        messageCascading,
                                        new MergeStringReplaceCallsFix(functionCall, previousCall)
                                    );
                                }
                            }
                        }

                        /* other cases */
                        this.checkNestedCalls(params[2], functionCall);
                        this.checkReplacementSimplification(params[1]);
                    }
                }
            }

            private void checkReplacementSimplification(@NotNull PsiElement replacementExpression) {
                if (replacementExpression instanceof ArrayCreationExpression) {
                    final Set<String> replacements = new HashSet<>();
                    for (final PsiElement oneReplacement : replacementExpression.getChildren()) {
                        if (oneReplacement instanceof PhpPsiElement) {
                            final PhpPsiElement item = ((PhpPsiElement) oneReplacement).getFirstPsiChild();
                            /* abort on non-string entries  */
                            if (!(item instanceof StringLiteralExpression)) {
                                return;
                            }
                            replacements.add(item.getText());
                        }
                    }
                    if (replacements.size() == 1) {
                        holder.registerProblem(
                            replacementExpression,
                            messageReplacements,
                            ProblemHighlightType.WEAK_WARNING,
                            new SimplifyReplacementFix(replacements.iterator().next())
                        );
                    }
                    replacements.clear();
                }
            }

            private void checkNestedCalls(@NotNull PsiElement callCandidate, @NotNull FunctionReference parentCall) {
                if (OpenapiTypesUtil.isFunctionReference(callCandidate)) {
                    final FunctionReference call = (FunctionReference) callCandidate;
                    final String functionName    = call.getName();
                    if (functionName != null && functionName.equals("str_replace")) {
                        holder.registerProblem(
                            callCandidate,
                            messageNesting,
                            new MergeStringReplaceCallsFix(parentCall, call)
                        );
                    }
                }
            }

            @Nullable
            private FunctionReference getStrReplaceReference(@NotNull AssignmentExpression assignment) {
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
            private AssignmentExpression getPreviousAssignment(@NotNull AssignmentExpression assignmentExpression) {
                /* get previous non-comment, non-php-doc expression */
                PsiElement previous = assignmentExpression.getParent().getPrevSibling();
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

    private class SimplifyReplacementFix extends UseSuggestedReplacementFixer {
        @NotNull
        @Override
        public String getName() {
            return "Simplify replacement definition";
        }

        SimplifyReplacementFix(@NotNull String expression) {
            super(expression);
        }
    }

    private static class MergeStringReplaceCallsFix implements LocalQuickFix {
        final private SmartPsiElementPointer<FunctionReference> patch;
        final private SmartPsiElementPointer<FunctionReference> eliminate;

        MergeStringReplaceCallsFix(@NotNull FunctionReference patch, @NotNull FunctionReference eliminate) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(patch.getProject());

            this.patch     = factory.createSmartPsiElementPointer(patch);
            this.eliminate = factory.createSmartPsiElementPointer(eliminate);
        }

        @NotNull
        @Override
        public String getName() {
            return "Merge str_replace(...) calls";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final FunctionReference patch     = this.patch.getElement();
            final FunctionReference eliminate = this.eliminate.getElement();
            if (patch != null && eliminate != null) {
                synchronized (eliminate.getContainingFile()) {
                    this.mergeReplaces(patch, eliminate);
                    this.mergeArguments(patch.getParameters()[0], eliminate.getParameters()[0]);
                    this.mergeSources(patch, eliminate);
                }
            }
        }

        private void mergeArguments(@NotNull PsiElement to, @NotNull PsiElement from) {
            final Project project  = to.getProject();
            if (to instanceof StringLiteralExpression) {
                if (from instanceof StringLiteralExpression) {
                    final String pattern = "array (%1%, %2%)".replace("%2%", to.getText()).replace("%1%", from.getText());
                    to.replace(PhpPsiElementFactory.createPhpPsiFromText(project, ArrayCreationExpression.class, pattern));
                } else if (from instanceof ArrayCreationExpression) {
                    final PsiElement comma       = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, ",");
                    final String pattern         = "array (%1%)".replace("%1%", to.getText());
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
                }
            } else if (to instanceof ArrayCreationExpression) {
                final PsiElement comma      = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, ",");
                final PsiElement firstValue = ((ArrayCreationExpression) to).getFirstPsiChild();
                final PsiElement marker     = firstValue == null ? null : firstValue.getPrevSibling();
                if (comma != null && marker != null) {
                    if (from instanceof StringLiteralExpression) {
                        to.addAfter(comma, marker);
                        to.addAfter(from.copy(), marker);
                    } else if (from instanceof ArrayCreationExpression) {
                        final PsiElement[] values = from.getChildren();
                        ArrayUtils.reverse(values);
                        Arrays.stream(values).forEach(value -> {
                            to.addAfter(comma, marker);
                            to.addAfter(value.copy(), marker);
                        });
                    }
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

        private void mergeReplaces(@NotNull FunctionReference to, @NotNull FunctionReference from) {
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
                this.expandReplacement(to);
                this.expandReplacement(from);
                this.mergeArguments(to.getParameters()[1], from.getParameters()[1]);
            }
        }

        private void expandReplacement(@NotNull FunctionReference call) {
            final PsiElement[] arguments = call.getParameters();
            final PsiElement search      = arguments[0];
            final PsiElement replace     = arguments[1];
            if (replace instanceof StringLiteralExpression && search instanceof ArrayCreationExpression) {
                final int searchesCount = search.getChildren().length;
                if (searchesCount > 1) {
                    final List<String> replaces = Collections.nCopies(searchesCount, replace.getText());
                    final String pattern        = "array (%a%)".replace("%a%", String.join(", ", replaces));
                    replace.replace(
                        PhpPsiElementFactory.createPhpPsiFromText(call.getProject(), ArrayCreationExpression.class, pattern)
                    );
                }
            }
        }

        private void mergeSources(@NotNull FunctionReference patch, @NotNull FunctionReference eliminate) {
            final PsiElement eliminateParent = eliminate.getParent().getParent();
            patch.getParameters()[2].replace(eliminate.getParameters()[2]);
            if (eliminateParent instanceof StatementImpl) {
                final PsiElement trailingSpaceCandidate = eliminateParent.getNextSibling();
                if (trailingSpaceCandidate instanceof PsiWhiteSpace) {
                    trailingSpaceCandidate.delete();
                }
                eliminateParent.delete();
            }
        }
    }
}
