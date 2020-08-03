package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SlowArrayOperationsInLoopInspector extends BasePhpInspection {
    private static final String messageGreedyPattern = "'%s(...)' is used in a loop and is a resources greedy construction.";
    private static final String messageSlowPattern   = "'%s(...)' is used in a loop and is a low performing construction.";

    @NotNull
    @Override
    public String getShortName() {
        return "SlowArrayOperationsInLoopInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Slow array function used in loop";
    }

    private static final Set<String> greedyFunctions = new HashSet<>();
    private static final Set<String> slowFunctions   = new HashSet<>();
    static {
        greedyFunctions.add("array_merge");
        greedyFunctions.add("array_merge_recursive");
        greedyFunctions.add("array_replace");
        greedyFunctions.add("array_replace_recursive");

        slowFunctions.add("count");
        slowFunctions.add("size");
        slowFunctions.add("strlen");
        slowFunctions.add("mb_strlen");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName = reference.getName();
                if (functionName != null && greedyFunctions.contains(functionName)) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length > 1 && !(arguments[0] instanceof ArrayAccessExpression)) {
                        PsiElement parent = reference.getParent();
                        if (parent instanceof AssignmentExpression) {
                            /* false-positives: return/break as last group statement expression */
                            boolean canLoop = true;
                            if (OpenapiTypesUtil.isStatementImpl(parent = parent.getParent())) {
                                final PsiElement grandParent = parent.getParent();
                                if (grandParent instanceof GroupStatement) {
                                    final PsiElement last = ExpressionSemanticUtil.getLastStatement((GroupStatement) grandParent);
                                    canLoop = !(last instanceof PhpBreak) && !(last instanceof PhpReturn);
                                }
                            }
                            while (canLoop && parent != null && !(parent instanceof PhpFile) && !(parent instanceof Function)) {
                                if (OpenapiTypesUtil.isLoop(parent)) {
                                    final PsiElement container = ((AssignmentExpression) reference.getParent()).getVariable();
                                    if (container != null) {
                                        for (final PsiElement argument : arguments) {
                                            if (OpenapiEquivalenceUtil.areEqual(container, argument)) {
                                                holder.registerProblem(
                                                        reference,
                                                        String.format(MessagesPresentationUtil.prefixWithEa(messageGreedyPattern), functionName)
                                                );
                                                return;
                                            }
                                        }
                                    }
                                }
                                parent = parent.getParent();
                            }
                        }
                    }
                }
            }

            @Override
            public void visitPhpFor(@NotNull For forStatement) {
                final Set<FunctionReference> references = new HashSet<>();
                Arrays.stream(forStatement.getConditionalExpressions()).forEach(c -> {
                    if (c instanceof BinaryExpression) {
                        final BinaryExpression binary = (BinaryExpression) c;
                        Stream.of(binary.getLeftOperand(), binary.getRightOperand())
                                .filter(p  -> p instanceof FunctionReference)
                                .forEach(p -> references.add((FunctionReference) p));
                    }
                });
                references.stream()
                        .filter(OpenapiTypesUtil::isFunctionReference)
                        .forEach(r -> {
                            final String functionName = r.getName();
                            if (functionName != null && slowFunctions.contains(functionName)) {
                                final BinaryExpression condition = (BinaryExpression) r.getParent();
                                holder.registerProblem(
                                        condition,
                                        String.format(MessagesPresentationUtil.prefixWithEa(messageSlowPattern), functionName),
                                        ProblemHighlightType.GENERIC_ERROR,
                                        new ReduceRepetitiveCallsInForFix(holder.getProject(), forStatement, condition)
                                );
                            }
                        });
                references.clear();
            }
        };
    }

    private static final class ReduceRepetitiveCallsInForFix implements LocalQuickFix {
        private static final String title = "Reduce the repetitive calls";

        private final SmartPsiElementPointer<For> forStatement;
        private final SmartPsiElementPointer<BinaryExpression> condition;

        ReduceRepetitiveCallsInForFix(@NotNull Project project, @NotNull For forStatement, @NotNull BinaryExpression condition) {
            super();
            final SmartPointerManager factory = SmartPointerManager.getInstance(project);

            this.forStatement = factory.createSmartPsiElementPointer(forStatement);
            this.condition    = factory.createSmartPsiElementPointer(condition);
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
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            final For forStatement           = this.forStatement.getElement();
            final BinaryExpression condition = this.condition.getElement();
            if (forStatement == null || condition == null || project.isDisposed()) {
                return;
            }

            final boolean functionOnLeft        = condition.getLeftOperand() instanceof FunctionReference;
            final PsiElement referenceCandidate = functionOnLeft ? condition.getLeftOperand() : condition.getRightOperand();
            final PsiElement variableCandidate  = functionOnLeft ? condition.getRightOperand() : condition.getLeftOperand();
            if (variableCandidate == null || referenceCandidate == null) {
                return;
            }

            String variableName             = (variableCandidate instanceof Variable) ? ((Variable) variableCandidate).getName() : "loops";
            variableName                    = '$' + variableName + "Max";
            final Variable variableElement  = PhpPsiElementFactory.createFromText(project, Variable.class, variableName);
            final AssignmentExpression init = PhpPsiElementFactory.createFromText(project, AssignmentExpression.class, variableName + " = " + referenceCandidate.getText());
            if (variableElement == null || init == null) {
                return;
            }

            referenceCandidate.replace(variableElement);

            // Case #1 and #2: have at least one initial expression.
            final PhpPsiElement[] initialExpressions = forStatement.getInitialExpressions();
            if (initialExpressions.length > 0) {
                final LeafPsiElement commaBeforeInitializer = PhpPsiElementFactory.createFromText(project, LeafPsiElement.class, ",");
                if (commaBeforeInitializer != null) {
                    final PhpPsiElement lastExpression = initialExpressions[initialExpressions.length - 1];
                    forStatement.addAfter(init, lastExpression);
                    forStatement.addAfter(commaBeforeInitializer, lastExpression);
                }
                return;
            }

            // Case #3: don't have any initial expression (eg. for(; ...)).
            // As For.class have no way to access the initial expression "container" when it is empty, then we need hard code that.
            for (final LeafPsiElement leaf : PsiTreeUtil.findChildrenOfType(forStatement, LeafPsiElement.class)) {
                if (leaf.getElementType() == PhpTokenTypes.chLPAREN) {
                    forStatement.addAfter(init, leaf);
                    break;
                }
            }
        }
    }
}