package com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.AssignmentExpression;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class OpAssignShortSyntaxInspector extends BasePhpInspection {
    private static final String messagePattern = "Can be safely refactored as '%s'.";

    @NotNull
    @Override
    public String getShortName() {
        return "OpAssignShortSyntaxInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Short syntax for applied operation";
    }

    private static final Map<IElementType, IElementType> mapping = new HashMap<>();
    private static final Set<IElementType> chainingSafeOperators = new HashSet<>();
    static {
        mapping.put(PhpTokenTypes.opPLUS,        PhpTokenTypes.opPLUS_ASGN);
        mapping.put(PhpTokenTypes.opMINUS,       PhpTokenTypes.opMINUS_ASGN);
        mapping.put(PhpTokenTypes.opMUL,         PhpTokenTypes.opMUL_ASGN);
        mapping.put(PhpTokenTypes.opDIV,         PhpTokenTypes.opDIV_ASGN);
        mapping.put(PhpTokenTypes.opREM,         PhpTokenTypes.opREM_ASGN);
        mapping.put(PhpTokenTypes.opCONCAT,      PhpTokenTypes.opCONCAT_ASGN);
        mapping.put(PhpTokenTypes.opBIT_AND,     PhpTokenTypes.opBIT_AND_ASGN);
        mapping.put(PhpTokenTypes.opBIT_OR,      PhpTokenTypes.opBIT_OR_ASGN);
        mapping.put(PhpTokenTypes.opBIT_XOR,     PhpTokenTypes.opBIT_XOR_ASGN);
        mapping.put(PhpTokenTypes.opSHIFT_LEFT,  PhpTokenTypes.opSHIFT_LEFT_ASGN);
        mapping.put(PhpTokenTypes.opSHIFT_RIGHT, PhpTokenTypes.opSHIFT_RIGHT_ASGN);

        chainingSafeOperators.add(PhpTokenTypes.opPLUS);
        chainingSafeOperators.add(PhpTokenTypes.opCONCAT);
        chainingSafeOperators.add(PhpTokenTypes.opMUL);
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpAssignmentExpression(@NotNull AssignmentExpression assignment) {
                final PsiElement value = ExpressionSemanticUtil.getExpressionTroughParenthesis(assignment.getValue());
                if (value instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) value;
                    final PsiElement operator     = binary.getOperation();
                    if (operator != null) {
                        final PsiElement left     = binary.getLeftOperand();
                        final PsiElement right    = binary.getRightOperand();
                        final PsiElement variable = assignment.getVariable();
                        if (variable != null && left != null && right != null) {
                            final IElementType operation = operator.getNode().getElementType();
                            if (mapping.containsKey(operation)) {
                                final LinkedList<PsiElement> fragments = new LinkedList<>();
                                fragments.addLast(right);
                                PsiElement candidate = left;
                                while (candidate instanceof BinaryExpression) {
                                    final BinaryExpression current = (BinaryExpression) candidate;
                                    final PsiElement rightPart     = current.getRightOperand();
                                    if (rightPart != null) {
                                        fragments.addLast(rightPart);
                                    }
                                    if (current.getOperationType() != operation) {
                                        break;
                                    }
                                    candidate = current.getLeftOperand();
                                }
                                if (candidate != null && OpenapiEquivalenceUtil.areEqual(variable, candidate)) {
                                    final boolean canShorten = (fragments.size() == 1 || chainingSafeOperators.contains(operation)) && fragments.stream().noneMatch(f -> f instanceof BinaryExpression);
                                    if (canShorten) {
                                        /* false-positives: string elements manipulation, causes a fatal error */
                                        boolean isStringManipulation = false;
                                        if (variable instanceof ArrayAccessExpression) {
                                            final PsiElement stringCandidate = ((ArrayAccessExpression) variable).getValue();
                                            if (stringCandidate instanceof PhpTypedElement) {
                                                final PhpType resolved = OpenapiResolveUtil.resolveType((PhpTypedElement) stringCandidate, holder.getProject());
                                                if (resolved != null && ! resolved.hasUnknown()) {
                                                    isStringManipulation = resolved.getTypes().stream().anyMatch(t -> Types.getType(t).equals(Types.strString));
                                                }
                                            }
                                        }
                                        if (! isStringManipulation) {
                                            Collections.reverse(fragments);
                                            final String replacement = String.format(
                                                    "%s %s= %s",
                                                    candidate.getText(),
                                                    operator.getText(),
                                                    fragments.stream().map(PsiElement::getText).collect(Collectors.joining(" " + operator.getText() + " "))
                                            );
                                            holder.registerProblem(
                                                    assignment,
                                                    String.format(MessagesPresentationUtil.prefixWithEa(messagePattern), replacement),
                                                    new UseShorthandOperatorFix(replacement)
                                            );
                                        }
                                    }
                                }
                                fragments.clear();
                            }
                        }
                    }
                }
            }
        };
    }

    private static final class UseShorthandOperatorFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use the short notation";

        UseShorthandOperatorFix(@NotNull String suggestedReplacement) {
            super(suggestedReplacement);
        }

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }
    }
}
