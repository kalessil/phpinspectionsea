package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.BinaryExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MkdirRaceConditionInspector extends BasePhpInspection {
    private static final String strProblemMkdirDirectCall  = "Following construct shall be used: 'if (!@mkdir(...) && !is_dir(...)) { throw ...; }'";
    private static final String strProblemMkdirInCondition = "Condition needs to be corrected (invert if needed): '!@mkdir(...) && !is_dir(...)'";

    @NotNull
    public String getShortName() {
        return "MkdirRaceConditionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                String functionName = reference.getName();
                if (!reference.getContainingFile().isValid() || StringUtil.isEmpty(functionName) || !functionName.equals("mkdir")) {
                    return;
                }

                /* ind out expression where the call is contained - quite big set of variations */
                final PsiElement parent = getCompleteExpression(reference);

                // case 1: [$var =] mkdir(...); / if ([!]mkdir(...))
                if (parent instanceof StatementImpl || parent instanceof AssignmentExpression || parent instanceof If) {
                    final PsiElement target = parent instanceof If ? parent.getFirstChild() : parent;
                    holder.registerProblem(target, strProblemMkdirDirectCall, ProblemHighlightType.GENERIC_ERROR);

                    return;
                }

                // case 2: && and || expressions
                if (parent.getParent() instanceof BinaryExpressionImpl) {
                    boolean isSecondExistenceCheckExists = false;

                    /* deal with nested conditions */
                    BinaryExpressionImpl binary = (BinaryExpressionImpl) parent.getParent();
                    if (binary.getRightOperand() == parent && binary.getParent() instanceof BinaryExpressionImpl) {
                        binary = (BinaryExpressionImpl) binary.getParent();
                    }

                    /* check if following expression contains is_dir */
                    Collection<FunctionReferenceImpl> calls = PsiTreeUtil.findChildrenOfType(binary.getRightOperand(), FunctionReferenceImpl.class);
                    for (FunctionReferenceImpl call : calls) {
                        final String name = call.getName();
                        if (StringUtil.isEmpty(name)) {
                            continue;
                        }

                        if (name.equals("is_dir")) {
                            isSecondExistenceCheckExists = true;
                            break;
                        }
                    }
                    calls.clear();

                    /* report when needed */
                    if (!isSecondExistenceCheckExists) {
                        holder.registerProblem(parent, strProblemMkdirInCondition, ProblemHighlightType.GENERIC_ERROR);
                        // return;
                    }
                }
            }

            @NotNull
            private PsiElement getCompleteExpression(@NotNull PsiElement expression) {
                final PsiElement parent = expression.getParent();

                if (parent instanceof StatementImpl || parent instanceof AssignmentExpression) {
                    return parent;
                }
                if (parent instanceof ParenthesizedExpression) {
                    return getCompleteExpression(parent);
                }

                if (parent instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) parent;
                    if (null != unary.getOperation()) {
                        final IElementType operation = unary.getOperation().getNode().getElementType();
                        if (PhpTokenTypes.opSILENCE == operation || PhpTokenTypes.opNOT == operation) {
                            return getCompleteExpression(unary);
                        }
                    }
                }

                if (parent instanceof BinaryExpressionImpl) {
                    BinaryExpressionImpl binary = (BinaryExpressionImpl) parent;
                    if (null != binary.getOperation()) {
                        final IElementType operation = binary.getOperationType();
                        if (PhpTokenTypes.opAND == operation || PhpTokenTypes.opOR == operation) {
                            return expression;
                        }

                        return getCompleteExpression(binary);
                    }
                }

                return parent;
            }
        };
    }
}
