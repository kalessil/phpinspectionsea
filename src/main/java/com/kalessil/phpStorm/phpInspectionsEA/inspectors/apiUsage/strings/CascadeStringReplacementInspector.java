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
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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

public class CascadeStringReplacementInspector extends BasePhpInspection {
    // Inspection options.
    public boolean USE_SHORT_ARRAYS_SYNTAX = false;

    private static final String messageNesting      = "This str_replace(...) call can be merged with its parent.";
    private static final String messageCascading    = "This str_replace(...) call can be merged with the previous.";
    private static final String messageReplacements = "Can be replaced with the string from the array.";

    @NotNull
    @Override
    public String getShortName() {
        return "CascadeStringReplacementInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Cascading 'str_replace(...)' calls";
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
                            final PsiElement callResultStorage = expression instanceof AssignmentExpression
                                                                    ? ((AssignmentExpression) expression).getVariable()
                                                                    : callSubject;
                            if (
                                callResultStorage != null && callSubject.getName().equals(previousVariable.getName()) &&
                                OpenapiEquivalenceUtil.areEqual(transitionVariable, callResultStorage) &&
                                this.canMergeArguments(functionCall, previousCall)
                            ) {
                                holder.registerProblem(
                                        functionCall,
                                        MessagesPresentationUtil.prefixWithEa(messageCascading),
                                        new MergeStringReplaceCallsFix(holder.getProject(), functionCall, previousCall, USE_SHORT_ARRAYS_SYNTAX)
                                );
                            }
                        }
                    }

                    /* case: nested replacements */
                    this.checkNestedCalls(holder.getProject(), arguments[2], functionCall);

                    /* case: search simplification */
                    if (arguments[1] instanceof StringLiteralExpression){
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
                        if (! (item instanceof StringLiteralExpression)) {
                            replacements.clear();
                            return;
                        }
                        replacements.add(item.getText());
                    }
                }
                if (replacements.size() == 1) {
                    holder.registerProblem(
                            candidate,
                            MessagesPresentationUtil.prefixWithEa(messageReplacements),
                            ProblemHighlightType.WEAK_WARNING,
                            new SimplifyReplacementFix(replacements.iterator().next())
                    );
                }
                replacements.clear();
            }

            private void checkNestedCalls(@NotNull Project project, @NotNull PsiElement callCandidate, @NotNull FunctionReference parentCall) {
                if (OpenapiTypesUtil.isFunctionReference(callCandidate)) {
                    final FunctionReference functionCall = (FunctionReference) callCandidate;
                    final String functionName            = functionCall.getName();
                    if (functionName != null && functionName.equals("str_replace") && this.canMergeArguments(functionCall, parentCall)) {
                        holder.registerProblem(
                                callCandidate,
                                MessagesPresentationUtil.prefixWithEa(messageNesting),
                                new MergeStringReplaceCallsFix(project, parentCall, functionCall, USE_SHORT_ARRAYS_SYNTAX)
                        );
                    }
                }
            }

            private boolean canMergeArguments(@NotNull FunctionReference call, @NotNull FunctionReference previousCall) {
                final PsiElement[] arguments         = call.getParameters();
                final PsiElement[] previousArguments = previousCall.getParameters();

                // If an argument is array (non-implicit), we need PHP 7.4+ for QF
                final boolean haveArrayType = Stream.of(arguments[0], arguments[1], previousArguments[0], previousArguments[1])
                      .filter(a   -> a instanceof PhpTypedElement && ! (a instanceof ArrayCreationExpression))
                      .anyMatch(a -> {
                          final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) a, holder.getProject());
                          if (resolved != null) {
                              final Set<String> types = resolved.filterUnknown().getTypes().stream().map(Types::getType).collect(Collectors.toSet());
                              return types.contains(Types.strArray) && ! types.contains(Types.strString);
                          }
                          return false;
                      });
                if (haveArrayType) {
                    return PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP740);
                }

                return true;
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
                final PsiElement value   = ExpressionSemanticUtil.getExpressionTroughParenthesis(ExpressionSemanticUtil.getReturnValue(phpReturn));
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
                while (previous != null && ! (previous instanceof PhpPsiElement)) {
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
            return MessagesPresentationUtil.prefixWithEa(title);
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

        MergeStringReplaceCallsFix(@NotNull Project project, @NotNull FunctionReference patch, @NotNull FunctionReference eliminate, boolean useShortSyntax) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);

            this.patch          = factory.createSmartPsiElementPointer(patch);
            this.eliminate      = factory.createSmartPsiElementPointer(eliminate);
            this.useShortSyntax = useShortSyntax;
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
            final FunctionReference patch     = this.patch.getElement();
            final FunctionReference eliminate = this.eliminate.getElement();
            if (patch != null && eliminate != null && !project.isDisposed()) {
                synchronized (eliminate.getContainingFile()) {
                    this.mergeReplaces(project, patch, eliminate);
                    this.mergeArguments(project, patch.getParameters()[0], eliminate.getParameters()[0]);
                    this.mergeSources(patch, eliminate);
                    this.applyArraySyntax(project, patch, this.useShortSyntax);
                }
            }
        }

        private void mergeArguments(@NotNull Project project, @NotNull PsiElement to, @NotNull PsiElement from) {
            final PsiElement comma = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, ",");
            if (to instanceof ArrayCreationExpression) {
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
                        final String pattern                  = String.format("array(%s%s)", this.needsDestructuring(project, from) ? "..." : "", from.getText());
                        final ArrayCreationExpression element = PhpPsiElementFactory.createPhpPsiFromText(project, ArrayCreationExpression.class, pattern);
                        to.addAfter(comma, marker);
                        to.addAfter(element.getFirstPsiChild(), marker);
                    }
                }
            } else {
                if (from instanceof ArrayCreationExpression) {
                    final String pattern                      = String.format("array(%s%s)", this.needsDestructuring(project, to) ? "..." : "", to.getText());
                    final ArrayCreationExpression replacement = PhpPsiElementFactory.createPhpPsiFromText(project, ArrayCreationExpression.class, pattern);
                    final PsiElement firstValue               = replacement.getFirstPsiChild();
                    final PsiElement marker                   = firstValue == null ? null : firstValue.getPrevSibling();
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
                    final String pattern = String.format(
                            "array(%s%s, %s%s)",
                            this.needsDestructuring(project, from) ? "..." : "", from.getText(),
                            this.needsDestructuring(project, to) ? "..." : "", to.getText()
                    );
                    to.replace(PhpPsiElementFactory.createPhpPsiFromText(project, ArrayCreationExpression.class, pattern));
                }
            }
        }

        private boolean needsDestructuring(@NotNull Project project, @NotNull PsiElement argument) {
            // Based on analysis pre-requisites we consider the destructuring available
            if (argument instanceof PhpTypedElement && ! (argument instanceof ArrayCreationExpression)) {
                final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) argument, project);
                if (resolved != null) {
                    final Set<String> types = resolved.filterUnknown().getTypes().stream().map(Types::getType).collect(Collectors.toSet());
                    return types.contains(Types.strArray) && ! types.contains(Types.strString);
                }
            }
            return false;
        }

        @NotNull
        private PsiElement unboxIfOneElementArray(@NotNull PsiElement what) {
            if (what instanceof ArrayCreationExpression) {
                final PsiElement[] elements = what.getChildren();
                if (elements.length == 1 && ! (elements[0] instanceof ArrayHashElement)) {
                    final PsiElement value = elements[0].getFirstChild();
                    if (value instanceof StringLiteralExpression) {
                        what = value;
                    }
                }
            }
            return what;
        }

        @NotNull
        private PsiElement unboxIfConstant(@NotNull PsiElement what) {
            PsiElement result = what;
            if (what instanceof ConstantReference) {
                final Set<PsiElement> variants = PossibleValuesDiscoveryUtil.discover(what);
                if (variants.size() == 1) {
                    result = variants.iterator().next();
                }
                variants.clear();
            }
            return result;
        }

        private void mergeReplaces(@NotNull Project project, @NotNull FunctionReference to, @NotNull FunctionReference from) {
            /* normalization here */
            final PsiElement fromNormalized = this.unboxIfOneElementArray(from.getParameters()[1]);
            final PsiElement toRaw          = to.getParameters()[1];
            final PsiElement toNormalized   = this.unboxIfOneElementArray(toRaw);

            /* a little bit of intelligence */
            boolean needsFurtherFixing = true;
            if (
                this.unboxIfConstant(toNormalized) instanceof StringLiteralExpression &&
                this.unboxIfConstant(fromNormalized) instanceof StringLiteralExpression &&
                OpenapiEquivalenceUtil.areEqual(fromNormalized, toNormalized)
            ) {
                toRaw.replace(toNormalized);
                needsFurtherFixing = false;
            }

            if (needsFurtherFixing) {
                /* in order to perform the proper merging we'll need to expand short-hand replacement definitions */
                this.expandReplacement(project, to);
                this.expandReplacement(project, from);
                this.mergeArguments(project, to.getParameters()[1], from.getParameters()[1]);
            }
        }

        private void expandReplacement(@NotNull Project project, @NotNull FunctionReference call) {
            final PsiElement[] arguments = call.getParameters();
            final PsiElement search      = arguments[0];
            final PsiElement replace     = arguments[1];
            if (this.unboxIfConstant(replace) instanceof StringLiteralExpression && search instanceof ArrayCreationExpression) {
                final int searchesCount = search.getChildren().length;
                if (searchesCount > 1) {
                    final List<String> replaces = Collections.nCopies(searchesCount, replace.getText());
                    replace.replace(
                        PhpPsiElementFactory.createPhpPsiFromText(
                            project,
                            ArrayCreationExpression.class,
                            String.format("array(%s)", String.join(", ", replaces))
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

        private void applyArraySyntax(@NotNull Project project, @NotNull FunctionReference patch, boolean useShortSyntax) {
            final List<String> arguments = new ArrayList<>();
            for (final PsiElement argument : patch.getParameters()) {
                if (argument instanceof ArrayCreationExpression) {
                    if (((ArrayCreationExpression) argument).isShortSyntax() != useShortSyntax) {
                        arguments.add(
                                String.format(
                                        useShortSyntax ? "[%s]" : "array(%s)",
                                        Stream.of(argument.getChildren()).map(PsiElement::getText).collect(Collectors.joining(", "))
                                )
                        );
                        continue;
                    }
                }
                arguments.add(argument.getText());
            }

            final String replacement = String.format("%s%s(%s)", patch.getImmediateNamespaceName(), patch.getName(), String.join(", ", arguments));
            patch.replace(PhpPsiElementFactory.createPhpPsiFromText(project, FunctionReference.class, replacement));
            arguments.clear();
        }
    }
}
