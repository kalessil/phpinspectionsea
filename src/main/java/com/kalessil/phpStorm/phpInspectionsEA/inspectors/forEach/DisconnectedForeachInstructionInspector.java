package com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

public class DisconnectedForeachInstructionInspector extends BasePhpInspection {
    // Inspection options.
    public boolean SUGGEST_USING_CLONE = false;

    private static final String messageDisconnected = "This statement seems to be disconnected from its parent foreach.";
    private static final String messageUseClone     = "Objects should be created outside of a loop and cloned instead.";

    @NotNull
    @Override
    public String getShortName() {
        return "DisconnectedForeachInstructionInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Statement could be decoupled from foreach";
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
            @Override
            public void visitPhpForeach(@NotNull ForeachStatement foreach) {
                final GroupStatement foreachBody = ExpressionSemanticUtil.getGroupStatement(foreach);
                /* ensure foreach structure is ready for inspection */
                if (foreachBody != null) {
                    final PsiElement[] statements = foreachBody.getChildren();
                    if (statements.length > 0 && Stream.of(statements).anyMatch(s -> OpenapiTypesUtil.is(s, PhpElementTypes.HTML))) {
                        return;
                    }

                    /* pre-collect introduced and internally used variables */
                    final Set<String> allModifiedVariables = this.collectCurrentAndOuterLoopVariables(foreach);

                    final Map<PsiElement, Set<String>> instructionDependencies = new HashMap<>();
                    /* iteration 1 - investigate what are dependencies and influence */
                    for (final PsiElement oneInstruction : statements) {
                        if (oneInstruction instanceof PhpPsiElement && !(oneInstruction instanceof PsiComment)) {
                            final Set<String> individualDependencies = new HashSet<>();
                            instructionDependencies.put(oneInstruction, individualDependencies);
                            investigateInfluence((PhpPsiElement) oneInstruction, individualDependencies, allModifiedVariables);
                        }
                    }

                    /* iteration 2 - analyse dependencies */
                    for (final PsiElement oneInstruction : statements) {
                        if (oneInstruction instanceof PhpPsiElement && !(oneInstruction instanceof PsiComment)) {
                            boolean isDependOnModified = false;

                            /* check if any dependency is overridden */
                            final Set<String> individualDependencies = instructionDependencies.get(oneInstruction);
                            if (individualDependencies != null && !individualDependencies.isEmpty()) {
                                isDependOnModified = individualDependencies.stream().anyMatch(allModifiedVariables::contains);
                                individualDependencies.clear();
                            }

                            /* verify and report if violation detected */
                            if (!isDependOnModified) {
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
                                            oneInstruction instanceof ControlStatement ||
                                            oneInstruction instanceof Try ||
                                            oneInstruction instanceof PhpSwitch
                                                    ? oneInstruction.getFirstChild()
                                                    : oneInstruction;

                                    /* secure exceptions with '<?= ?>' constructions, false-positives with html */
                                    if (!OpenapiTypesUtil.isPhpExpressionImpl(oneInstruction) && oneInstruction.getTextLength() > 0) {
                                        /* inner looping termination/continuation should be taken into account */
                                        final PsiElement loopInterrupter
                                            = PsiTreeUtil.findChildOfAnyType(oneInstruction, true, PhpBreak.class, PhpContinue.class, PhpReturn.class, OpenapiPlatformUtil.classes.get("PhpThrow"));
                                        /* operating with variables should be taken into account */
                                        final boolean isVariablesUsed = PsiTreeUtil.findChildOfAnyType(oneInstruction, true, (Class) Variable.class) != null;
                                        if (null == loopInterrupter && isVariablesUsed) {
                                            holder.registerProblem(
                                                    reportingTarget,
                                                    MessagesPresentationUtil.prefixWithEa(messageDisconnected)
                                            );
                                        }
                                    }
                                }

                                if (SUGGEST_USING_CLONE && (ExpressionType.DOM_ELEMENT_CREATE == target || ExpressionType.NEW == target)) {
                                    holder.registerProblem(
                                            oneInstruction,
                                            MessagesPresentationUtil.prefixWithEa(messageUseClone)
                                    );
                                }
                            }
                        }
                    }

                    /* release containers content */
                    allModifiedVariables.clear();
                    instructionDependencies.values().forEach(Set::clear);
                    instructionDependencies.clear();
                }
            }

            private Set<String> collectCurrentAndOuterLoopVariables(@NotNull ForeachStatement foreach) {
                final Set<String> variables = new HashSet<>();
                PsiElement current          = foreach;
                while (current != null && !(current instanceof Function) && !(current instanceof PsiFile)) {
                    if (current instanceof ForeachStatement) {
                        ((ForeachStatement) current).getVariables().forEach(v -> variables.add(v.getName()));
                    }
                    current = current.getParent();
                }
                return variables;
            }

            private void investigateInfluence(
                @Nullable PhpPsiElement oneInstruction,
                @NotNull Set<String> individualDependencies,
                @NotNull Set<String> allModifiedVariables
            ) {
                for (final Variable variable : PsiTreeUtil.findChildrenOfType(oneInstruction, Variable.class)) {
                    final String variableName = variable.getName();
                    PsiElement valueContainer = variable;
                    PsiElement parent         = variable.getParent();
                    while (parent instanceof FieldReference) {
                        valueContainer = parent;
                        parent         = parent.getParent();
                    }
                    /* a special case: `[] = ` and `array() = ` unboxing */
                    if (OpenapiTypesUtil.is(parent, PhpElementTypes.ARRAY_VALUE)) {
                        parent = parent.getParent();
                        if (parent instanceof ArrayCreationExpression) {
                            parent = parent.getParent();
                        }
                    }
                    final PsiElement grandParent = parent.getParent();

                    /* writing into variable */
                    if (parent instanceof AssignmentExpression) {
                        /* php-specific `list(...) =` , `[...] =` construction */
                        if (parent instanceof MultiassignmentExpression) {
                            final MultiassignmentExpression assignment = (MultiassignmentExpression) parent;
                            if (assignment.getValue() != variable) {
                                allModifiedVariables.add(variableName);
                                individualDependencies.add(variableName);
                                continue;
                            }
                        } else {
                            final AssignmentExpression assignment = (AssignmentExpression) parent;
                            if (assignment.getVariable() == valueContainer) {
                                /* we are modifying the variable */
                                allModifiedVariables.add(variableName);
                                /* self-assignment and field assignment counted as the variable dependent on itself  */
                                if (assignment instanceof SelfAssignmentExpression || valueContainer instanceof FieldReference) {
                                    individualDependencies.add(variableName);
                                }
                                /* assignments as call arguments counted as the variable dependent on itself */
                                if (grandParent instanceof ParameterList) {
                                    individualDependencies.add(variableName);
                                }
                                continue;
                            }
                        }
                    }

                    /* adding into an arrays; we both depend and modify the container */
                    if (parent instanceof ArrayAccessExpression && valueContainer == ((ArrayAccessExpression) parent).getValue()) {
                        allModifiedVariables.add(variableName);
                        individualDependencies.add(variableName);
                    }

                    if (parent instanceof ParameterList) {
                        if (grandParent instanceof MethodReference) {
                            /* an object consumes the variable, perhaps modification takes place */
                            final MethodReference reference    = (MethodReference) grandParent;
                            final PsiElement referenceOperator = OpenapiPsiSearchUtil.findResolutionOperator(reference);
                            if (OpenapiTypesUtil.is(referenceOperator, PhpTokenTypes.ARROW)) {
                                final PsiElement variableCandidate = reference.getFirstPsiChild();
                                if (variableCandidate instanceof Variable) {
                                    allModifiedVariables.add(((Variable) variableCandidate).getName());
                                    continue;
                                }
                            }
                        } else if (OpenapiTypesUtil.isFunctionReference(grandParent)) {
                            /* php will create variable, if it is by reference */
                            final FunctionReference reference = (FunctionReference) grandParent;
                            final int position                = ArrayUtils.indexOf(reference.getParameters(), variable);
                            if (position != -1) {
                                final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                                if (resolved instanceof Function) {
                                    final Parameter[] parameters = ((Function) resolved).getParameters();
                                    if (parameters.length > position && parameters[position].isPassByRef()) {
                                        allModifiedVariables.add(variableName);
                                        individualDependencies.add(variableName);
                                        continue;
                                    }
                                }
                            }
                        }
                    }

                    /* increment/decrement are also write operations */
                    final ExpressionType type = this.getExpressionType(parent);
                    if (ExpressionType.INCREMENT == type || ExpressionType.DECREMENT == type) {
                        allModifiedVariables.add(variableName);
                        individualDependencies.add(variableName);
                        continue;
                    }
                    /* TODO: lookup for array access and property access */

                    individualDependencies.add(variableName);
                }

                /* handle compact function usage */
                for (final FunctionReference reference : PsiTreeUtil.findChildrenOfType(oneInstruction, FunctionReference.class)) {
                    if (OpenapiTypesUtil.isFunctionReference(reference)) {
                        final String functionName = reference.getName();
                        if (functionName != null && functionName.equals("compact")) {
                            for (final PsiElement argument : reference.getParameters()) {
                                if (argument instanceof StringLiteralExpression) {
                                    final String compactedVariableName = ((StringLiteralExpression) argument).getContents();
                                    if (!compactedVariableName.isEmpty()) {
                                        individualDependencies.add(compactedVariableName);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @NotNull
            private ExpressionType getExpressionType(@Nullable PsiElement expression) {
                if (expression instanceof PhpBreak || expression instanceof PhpContinue || expression instanceof PhpReturn) {
                    return ExpressionType.CONTROL_STATEMENTS;
                }

                /* regular '...;' statements */
                if (OpenapiTypesUtil.isStatementImpl(expression)) {
                    return getExpressionType(((Statement) expression).getFirstPsiChild());
                }

                /* unary operations */
                if (expression instanceof UnaryExpression) {
                    final PsiElement operation = ((UnaryExpression) expression).getOperation();
                    if (OpenapiTypesUtil.is(operation, PhpTokenTypes.opINCREMENT)) {
                        return ExpressionType.INCREMENT;
                    }
                    if (OpenapiTypesUtil.is(operation, PhpTokenTypes.opDECREMENT)) {
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
                        } else if (value instanceof UnaryExpression) {
                            if (OpenapiTypesUtil.is(((UnaryExpression) value).getOperation(), PhpTokenTypes.kwCLONE)) {
                                return ExpressionType.CLONE;
                            }
                        } else if (value instanceof MethodReference) {
                            final MethodReference call = (MethodReference) value;
                            final String methodName    = call.getName();
                            if (methodName != null && methodName.equals("createElement")) {
                                final PsiElement resolved = OpenapiResolveUtil.resolveReference(call);
                                if (
                                    resolved instanceof Method &&
                                    ((Method) resolved).getFQN().equals("\\DOMDocument.createElement")
                                ) {
                                    return ExpressionType.DOM_ELEMENT_CREATE;
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

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component)
            -> component.addCheckbox("Suggest using clone", SUGGEST_USING_CLONE, (isSelected) -> SUGGEST_USING_CLONE = isSelected));
    }
}
