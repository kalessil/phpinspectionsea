package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FileSystemUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MkdirRaceConditionInspector extends BasePhpInspection {
    private static final String patternMkdirDirectCall   = "Following construct should be used: 'if (!mkdir(%f%) && !is_dir(%f%)) { ... }'.";
    private static final String patternMkdirAndCondition = "Some check are missing: '!mkdir(%f%) && !is_dir(%f%)'.";
    private static final String patternMkdirOrCondition  = "Some check are missing: 'mkdir(%f%) || is_dir(%f%)'.";

    @NotNull
    public String getShortName() {
        return "MkdirRaceConditionInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                final String functionName    = reference.getName();
                final PsiElement[] arguments = reference.getParameters();
                if (arguments.length != 1 || functionName == null || !functionName.equals("mkdir")) {
                    return;
                }

                /* ignore test classes */
                final Function scope = ExpressionSemanticUtil.getScope(reference);
                if (scope instanceof Method) {
                    final PhpClass clazz = ((Method) scope).getContainingClass();
                    if (null != clazz && FileSystemUtil.isTestClass(clazz)) {
                        return;
                    }
                }

                /* ind out expression where the call is contained - quite big set of variations */
                final PsiElement parent = this.getCompleteExpression(reference);

                // case 1: if ([!]mkdir(...))
                if (parent instanceof If || OpenapiTypesUtil.isStatementImpl(parent)) {
                    final PsiElement target = parent instanceof If ? ((If) parent).getCondition() : parent;
                    final String message =
                            (parent instanceof If ? patternMkdirAndCondition : patternMkdirDirectCall)
                            .replace("%f%", arguments[0].getText())
                            .replace("%f%", arguments[0].getText());
                    //noinspection ConstantConditions ; at this point the condition can not be null
                    holder.registerProblem(target, message);
                }
                // case 2: && and || expressions
                else if (parent.getParent() instanceof BinaryExpression) {
                    boolean isSecondExistenceCheckExists = false;

                    /* deal with nested conditions */
                    BinaryExpression binary = (BinaryExpression) parent.getParent();
                    if (binary.getRightOperand() == parent && binary.getParent() instanceof BinaryExpression) {
                        binary = (BinaryExpression) binary.getParent();
                    }

                    /* check if following expression contains is_dir */
                    final Collection<FunctionReference> calls
                            = PsiTreeUtil.findChildrenOfType(binary.getRightOperand(), FunctionReference.class);
                    for (final FunctionReference call : calls) {
                        final String name = call.getName();
                        if (name != null && name.equals("is_dir") && OpenapiTypesUtil.isFunctionReference(call)) {
                            isSecondExistenceCheckExists = true;
                            break;
                        }
                    }
                    calls.clear();

                    /* report when needed */
                    if (!isSecondExistenceCheckExists) {
                        final IElementType operation = binary.getOperationType();
                        final String message =
                                (operation == PhpTokenTypes.opAND ? patternMkdirAndCondition : patternMkdirOrCondition)
                                .replace("%f%", arguments[0].getText())
                                .replace("%f%", arguments[0].getText());
                        holder.registerProblem(parent, message);
                    }
                }
            }

            @NotNull
            private PsiElement getCompleteExpression(@NotNull PsiElement expression) {
                final PsiElement parent = expression.getParent();

                if (parent instanceof AssignmentExpression || OpenapiTypesUtil.isStatementImpl(parent)) {
                    return parent;
                } else if (parent instanceof ParenthesizedExpression) {
                    return this.getCompleteExpression(parent);
                } else if (parent instanceof UnaryExpression) {
                    final UnaryExpression unary = (UnaryExpression) parent;
                    final PsiElement operation  = unary.getOperation();
                    if (operation != null) {
                        final IElementType operator = operation.getNode().getElementType();
                        if (operator == PhpTokenTypes.opNOT || operator == PhpTokenTypes.opSILENCE) {
                            return this.getCompleteExpression(unary);
                        }
                    }
                } else if (parent instanceof BinaryExpression) {
                    final BinaryExpression binary = (BinaryExpression) parent;
                    final IElementType operation  = binary.getOperationType();
                    if (PhpTokenTypes.opAND == operation || PhpTokenTypes.opOR == operation) {
                        return expression;
                    }
                    return this.getCompleteExpression(binary);
                }

                return parent;
            }
        };
    }
}
