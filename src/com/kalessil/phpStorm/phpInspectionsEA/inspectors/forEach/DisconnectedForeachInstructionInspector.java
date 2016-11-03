package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import com.jetbrains.php.lang.psi.elements.impl.UnaryExpressionImpl;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DisconnectedForeachInstructionInspector extends BasePhpInspection {
    private static final String strProblemDescription = "This statement seems to be disconnected from parent foreach";
    private static final String strProblemUseClone    = "Objects should be created outside of a loop and cloned instead";

    @NotNull
    public String getShortName() {
        return "DisconnectedForeachInstructionInspection";
    }

    private enum ExpressionType {
        INCREMENT, DECREMENT,
        CLONE, NEW, DOM_ELEMENT_CREATE,
        CONTROL_STATEMENTS, ASSIGNMENT,
        ACCUMULATE_IN_ARRAY, OTHER
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpForeach(ForeachStatement foreach) {
                final Variable value             = foreach.getValue();
                final GroupStatement foreachBody = ExpressionSemanticUtil.getGroupStatement(foreach);
                /* ensure foreach structure is ready for inspection */
                if (null != foreachBody && null != value && !StringUtil.isEmpty(value.getName())) {
                    /* pre-collect introduced and internally used variables */
                    final HashSet<String> allModifiedVariables = new HashSet<String>();
                    allModifiedVariables.add(value.getName());
                    final Variable key = foreach.getKey();
                    if (null != key && !StringUtil.isEmpty(key.getName())) {
                        allModifiedVariables.add(key.getName());
                    }

                    final HashMap<PsiElement, HashSet<String>> instructionDependencies = new HashMap<PsiElement, HashSet<String>>();
                    /* iteration 1 - investigate what are dependencies and influence */
                    for (PsiElement oneInstruction : foreachBody.getStatements()) {
                        if (oneInstruction instanceof PhpPsiElement && !(oneInstruction instanceof PsiComment)) {
                            final HashSet<String> individualDependencies = new HashSet<String>();

                            instructionDependencies.put(oneInstruction, individualDependencies);
                            investigateInfluence((PhpPsiElement) oneInstruction, individualDependencies, allModifiedVariables);
                        }
                    }

                    /* iteration 2 - analyse dependencies */
                    for (PsiElement oneInstruction : foreachBody.getStatements()) {
                        if (oneInstruction instanceof PhpPsiElement && !(oneInstruction instanceof PsiComment)) {
                            boolean isDependOnModifiedVariables = false;

                            /* check if any dependency is overridden */
                            final HashSet<String> individualDependencies = instructionDependencies.get(oneInstruction);
                            if (null != individualDependencies && individualDependencies.size() > 0) {
                                /* contains not only this */
                                for (String dependencyName : individualDependencies) {
                                    if (allModifiedVariables.contains(dependencyName)) {
                                        isDependOnModifiedVariables = true;
                                        break;
                                    }
                                }
                            }

                            /* verify and report if violation detected */
                            if (!isDependOnModifiedVariables) {
                                final ExpressionType target = getExpressionType(oneInstruction);
                                if (
                                    ExpressionType.NEW                 != target &&
                                    ExpressionType.ASSIGNMENT          != target &&
                                    ExpressionType.CLONE               != target &&
                                    ExpressionType.INCREMENT           != target &&
                                    ExpressionType.DECREMENT           != target &&
                                    ExpressionType.DOM_ELEMENT_CREATE  != target &&
                                    ExpressionType.ACCUMULATE_IN_ARRAY != target &&
                                    ExpressionType.CONTROL_STATEMENTS  != target
                                ) {
                                    /* loops, ifs, switches, try's needs to be reported on keyword, others - complete */
                                    final PsiElement reportingTarget =
                                            (
                                                oneInstruction instanceof ControlStatement ||
                                                oneInstruction instanceof Try ||
                                                oneInstruction instanceof PhpSwitch
                                            )
                                                    ? oneInstruction.getFirstChild()
                                                    : oneInstruction;
                                    holder.registerProblem(reportingTarget, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
                                }

                                if (ExpressionType.DOM_ELEMENT_CREATE == target || ExpressionType.NEW == target) {
                                    holder.registerProblem(oneInstruction, strProblemUseClone, ProblemHighlightType.WEAK_WARNING);
                                }
                            }

                            /* cleanup dependencies details */
                            if (null != individualDependencies) {
                                individualDependencies.clear();
                            }
                        }
                    }
                    /* empty dependencies details container */
                    instructionDependencies.clear();
                }
            }

            private void investigateInfluence(
                @Nullable PhpPsiElement oneInstruction,
                @NotNull HashSet<String> individualDependencies,
                @NotNull HashSet<String> allModifiedVariables
            ) {
                for (PsiElement variable : PsiTreeUtil.findChildrenOfType(oneInstruction, Variable.class)) {
                    final String variableName = ((Variable) variable).getName();
                    if (!StringUtil.isEmpty(variableName)) {
                        PsiElement valueContainer = variable;
                        PsiElement parent         = variable.getParent();
                        while (parent instanceof FieldReference) {
                            valueContainer = parent;
                            parent         = parent.getParent();
                        }

                        /* writing into variable */
                        if (parent instanceof AssignmentExpression) {
                            final AssignmentExpression assignment = (AssignmentExpression) parent;
                            if (assignment.getVariable() == valueContainer) {
                                /* we are modifying the variable */
                                allModifiedVariables.add(variableName);
                                /* self-assignment and field assignment makes the variable dependent on itself  */
                                if (
                                    parent instanceof SelfAssignmentExpression ||
                                    valueContainer instanceof FieldReference
                                ) {
                                    individualDependencies.add(variableName);
                                }

                                continue;
                            }
                        }

                        /* php-specific list(...) = ... construction */
                        if (parent instanceof MultiassignmentExpression) {
                            final List<PhpPsiElement> variables = ((MultiassignmentExpression) parent).getVariables();
                            if (variables.size() > 0) {
                                if (variables.contains(variable)) {
                                    allModifiedVariables.add(variableName);

                                    variables.clear();
                                    continue;
                                }
                                variables.clear();
                            }
                        }

                        /* php-specific variables introduction: preg_match[_all] exporting results into 3rd argument */
                        if (parent instanceof ParameterList && parent.getParent() instanceof FunctionReference) {
                            final FunctionReference call  = (FunctionReference) parent.getParent();
                            final String functionName     = call.getName();
                            final PsiElement[] parameters = call.getParameters();

                            // TODO: array_pop, array_shift, next, current, fwrite... -> use mapping function => argument modified
                            if (
                                3 == parameters.length && parameters[2] == variable &&
                                !StringUtil.isEmpty(functionName) && functionName.startsWith("preg_match")
                            ) {
                                allModifiedVariables.add(variableName);
                                continue;
                            }
                        }

                        /* increment/decrement are also write operations */
                        final ExpressionType type = getExpressionType(parent);
                        if (ExpressionType.INCREMENT == type || ExpressionType.DECREMENT == type) {
                            allModifiedVariables.add(variableName);
                            individualDependencies.add(variableName);
                            continue;
                        }
                        /* TODO: lookup for array access and property access */

                        individualDependencies.add(variableName);
                    }
                }
            }

            @NotNull
            private ExpressionType getExpressionType(@Nullable PsiElement expression) {
                if (
                    expression instanceof PhpBreak || expression instanceof PhpContinue ||
                    expression instanceof PhpReturn
                ) {
                    return ExpressionType.CONTROL_STATEMENTS;
                }

                /* regular '...;' statements */
                if (expression instanceof StatementImpl) {
                    return getExpressionType(((StatementImpl) expression).getFirstPsiChild());
                }

                /* unary operations */
                if (expression instanceof UnaryExpressionImpl) {
                    final PsiElement operation = ((UnaryExpressionImpl) expression).getOperation();
                    IElementType operationType = null;
                    if (null != operation) {
                        operationType = operation.getNode().getElementType();
                    }
                    if (PhpTokenTypes.opINCREMENT == operationType) {
                        return ExpressionType.INCREMENT;
                    }
                    if (PhpTokenTypes.opDECREMENT == operationType) {
                        return ExpressionType.DECREMENT;
                    }
                }

                /* different types of assignments */
                if (expression instanceof AssignmentExpression) {
                    final AssignmentExpression assignment = (AssignmentExpression) expression;
                    final PsiElement variable             = assignment.getVariable();
                    if (variable instanceof Variable) {
                        final PsiElement value = assignment.getValue();
                        if (value instanceof NewExpression) {
                            return ExpressionType.NEW;
                        }

                        if (value instanceof UnaryExpressionImpl) {
                            final PsiElement operation = ((UnaryExpressionImpl) value).getOperation();
                            if (null != operation && PhpTokenTypes.kwCLONE == operation.getNode().getElementType()) {
                                return ExpressionType.CLONE;
                            }
                        }

                        /* TODO: configurable \DOMNode::appendChild, \DOMDocument::createTextNode */
                        if (value instanceof MethodReference) {
                            final MethodReference call = (MethodReference) value;
                            final String methodName    = call.getName();
                            if (!StringUtil.isEmpty(methodName) && methodName.equals("createElement")) {
                                final PsiElement resolved = call.resolve();
                                if (resolved instanceof Method) {
                                    final String fqn = ((Method) resolved).getFQN();
                                    if (!StringUtil.isEmpty(fqn) && fqn.equals("\\DOMDocument.createElement")) {
                                        return ExpressionType.DOM_ELEMENT_CREATE;
                                    }
                                }
                            }
                        }

                        /* allow all assignations afterwards */
                        return ExpressionType.ASSIGNMENT;
                    }

                    /* accumulating something in external container */
                    if (variable instanceof ArrayAccessExpression) {
                        final ArrayAccessExpression storage = (ArrayAccessExpression) variable;
                        if (null == storage.getIndex() || null == storage.getIndex().getValue()) {
                            return ExpressionType.ACCUMULATE_IN_ARRAY;
                        }
                    }
                }

                return ExpressionType.OTHER;
            }
        };
    }
}
