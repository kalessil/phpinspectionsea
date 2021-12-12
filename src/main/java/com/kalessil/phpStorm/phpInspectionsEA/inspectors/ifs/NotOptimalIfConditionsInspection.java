package com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.strategy.AndOrWordsUsageStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionCostEstimateUtil;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.utils.ExpressionsCouplingCheckUtil;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.hierarhy.InterfacesExtractUtil;
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

public class NotOptimalIfConditionsInspection extends BasePhpInspection {
    // Inspection options.
    public boolean REPORT_LITERAL_OPERATORS      = true;
    public boolean REPORT_INSTANCE_OF_FLAWS      = true;
    public boolean SUGGEST_OPTIMIZING_CONDITIONS = true;

    private static final String messageInstanceOfComplementarity = "Probable bug: ensure this behaves properly with 'instanceof(...)' in this scenario.";
    private static final String messageInstanceOfAmbiguous       = "This condition is ambiguous and can be safely removed.";
    private static final String messageOrdering                  = "This condition execution costs less than the previous one.";
    private static final String messageDuplicateConditionPart    = "This call is duplicated in conditions set.";

    @NotNull
    @Override
    public String getShortName() {
        return "NotOptimalIfConditionsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Non-optimal if conditions";
    }

    final private static Set<String> functionsSet = new HashSet<>();
    static {
        functionsSet.add("array_key_exists");
        functionsSet.add("function_exists");
        functionsSet.add("property_exists");
        functionsSet.add("class_exists");
        functionsSet.add("interface_exists");
        functionsSet.add("trait_exists");
        functionsSet.add("is_a");
        functionsSet.add("is_subclass_of");

        functionsSet.add("defined");
        functionsSet.add("is_array");
        functionsSet.add("is_bool");
        functionsSet.add("is_callable");
        functionsSet.add("is_countable");
        functionsSet.add("is_float");
        functionsSet.add("is_double");
        functionsSet.add("is_real");
        functionsSet.add("is_int");
        functionsSet.add("is_integer");
        functionsSet.add("is_long");
        functionsSet.add("is_iterable");
        functionsSet.add("is_null");
        functionsSet.add("is_numeric");
        functionsSet.add("is_object");
        functionsSet.add("is_resource");
        functionsSet.add("is_scalar");
        functionsSet.add("is_string");
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpIf(@NotNull If ifStatement) {
                final List<PsiElement> objAllConditions = new ArrayList<>();
                final IElementType[] arrOperationHolder = { null };

                List<PsiElement> objConditionsFromStatement = this.inspectExpressionsOrder(ifStatement.getCondition(), arrOperationHolder);
                if (null != objConditionsFromStatement) {
                    objAllConditions.addAll(objConditionsFromStatement);

                    if (REPORT_INSTANCE_OF_FLAWS) {
                        this.inspectConditionsForInstanceOfAndIdentityOperations(objConditionsFromStatement, arrOperationHolder[0]);
                        this.inspectConditionsForAmbiguousInstanceOf(objConditionsFromStatement);
                    }

                    objConditionsFromStatement.clear();

                    if (REPORT_LITERAL_OPERATORS) {
                        AndOrWordsUsageStrategy.apply(ifStatement.getCondition(), holder);
                    }
                }

                for (final ElseIf objElseIf : ifStatement.getElseIfBranches()) {
                    objConditionsFromStatement = this.inspectExpressionsOrder(objElseIf.getCondition(), arrOperationHolder);
                    if (objConditionsFromStatement != null) {
                        objAllConditions.addAll(objConditionsFromStatement);

                        if (REPORT_INSTANCE_OF_FLAWS) {
                            this.inspectConditionsForInstanceOfAndIdentityOperations(objConditionsFromStatement, arrOperationHolder[0]);
                            this.inspectConditionsForAmbiguousInstanceOf(objConditionsFromStatement);
                        }

                        objConditionsFromStatement.clear();

                        if (REPORT_LITERAL_OPERATORS) {
                            AndOrWordsUsageStrategy.apply(objElseIf.getCondition(), holder);
                        }
                    }
                }

                /* TODO: If not binary/ternary/assignment/array access expression,  */
                /* TODO: perform types lookup - nullable core types/classes should be compared with null.  */
                /* TODO: Inversion should be un-boxed to get expression. */

                objAllConditions.clear();
            }

            // reports $value instanceof \DateTime OP $value instanceof \DateTimeInterface
            private void inspectConditionsForAmbiguousInstanceOf(@NotNull List<PsiElement> conditions) {
                if (conditions.size() < 2) {
                    return;
                }

                // find all instanceof expressions
                final List<BinaryExpression> instanceOfExpressions = new ArrayList<>();
                conditions.stream()
                        .filter(expression -> expression instanceof BinaryExpression)
                        .forEach(expression -> {
                            final BinaryExpression binary = (BinaryExpression) expression;
                            if (PhpTokenTypes.kwINSTANCEOF == binary.getOperationType()) {
                                instanceOfExpressions.add(binary);
                            }
                        });
                // terminate processing if not enough entries for analysis
                if (instanceOfExpressions.size() < 2) {
                    instanceOfExpressions.clear();
                    return;
                }

                // now we need to build up following structure:
                /* 'subject' => [ condition => class, ... ] */
                final Map<PsiElement, Map<PsiElement, PhpClass>> mappedChecks = new HashMap<>();
                for (final BinaryExpression instanceOfExpression : instanceOfExpressions) {
                    // ensure expression is well-formed
                    final PsiElement subject = instanceOfExpression.getLeftOperand();
                    if (null == subject || !(instanceOfExpression.getRightOperand() instanceof ClassReference)) {
                        continue;
                    }

                    // ensure resolvable
                    final ClassReference reference = (ClassReference) instanceOfExpression.getRightOperand();
                    final PsiElement resolved      = OpenapiResolveUtil.resolveReference(reference);
                    if (!(resolved instanceof PhpClass)) {
                        continue;
                    }
                    final PhpClass clazz = (PhpClass) resolved;

                    // push subject properly, as expressions can be different objects with the same semantics
                    PsiElement registeredSubject = null;
                    for (final PsiElement testSubject : mappedChecks.keySet()) {
                        if (OpenapiEquivalenceUtil.areEqual(subject, testSubject)) {
                            registeredSubject = testSubject;
                            break;
                        }
                    }
                    // put empty container if it's not known
                    if (null == registeredSubject) {
                        mappedChecks.put(subject, new HashMap<>());
                        registeredSubject = subject;
                    }

                    // register condition for further analysis
                    mappedChecks.get(registeredSubject).put(instanceOfExpression, clazz);
                }
                // release references in the raw list
                instanceOfExpressions.clear();

                final boolean isDateTimeInterfaceAvailable = PhpLanguageLevel.get(holder.getProject()).atLeast(PhpLanguageLevel.PHP550);

                // process entries, perform subject container clean up on each iteration
                final Map<PhpClass, Set<PhpClass>> resolvedInheritanceChains = new HashMap<>();
                for (final Map<PsiElement, PhpClass> subjectContainer : mappedChecks.values()) {
                    // investigate one subject when it has multiple instanceof-expressions
                    if (subjectContainer.size() > 1) {
                        // walk through conditions
                        for (Map.Entry<PsiElement, PhpClass> instanceOf2class: subjectContainer.entrySet()) {
                            /* unpack the pair */
                            final PhpClass clazz                  = instanceOf2class.getValue();
                            final PsiElement instanceOfExpression = instanceOf2class.getKey();

                            // extract current condition details
                            final Set<PhpClass> clazzParents = resolvedInheritanceChains
                                .computeIfAbsent(clazz, c -> InterfacesExtractUtil.getCrawlInheritanceTree(c, true));

                            // inner loop for verification
                            for (Map.Entry<PsiElement, PhpClass> instanceOf2classInner : subjectContainer.entrySet()) {
                                // skip itself
                                if (instanceOf2classInner.getKey() == instanceOfExpression) {
                                    continue;
                                }

                                // if alternative references to base class current check is ambiguous
                                final PhpClass secondClass = instanceOf2classInner.getValue();
                                if (clazzParents.contains(secondClass)) {
                                    /* false-positive: the interface in stubs but accessible in php 5.5+ only */
                                    if (secondClass.getFQN().equals("\\DateTimeInterface") && !isDateTimeInterfaceAvailable) {
                                        continue;
                                    }

                                    holder.registerProblem(
                                            instanceOfExpression,
                                            MessagesPresentationUtil.prefixWithEa(messageInstanceOfAmbiguous),
                                            ProblemHighlightType.LIKE_UNUSED_SYMBOL
                                    );
                                    break;
                                }
                            }
                        }
                    }
                    subjectContainer.clear();
                }
                // release inheritance cache as well
                resolvedInheritanceChains.values().forEach(Set::clear);
                resolvedInheritanceChains.clear();
                // release mapping as well
                mappedChecks.clear();
            }

            /* TODO: is_* functions */
            private void inspectConditionsForInstanceOfAndIdentityOperations(@NotNull List<PsiElement> conditions, @Nullable IElementType operationType) {
                PsiElement testSubject = null;
                if (operationType == PhpTokenTypes.opAND && conditions.size() > 1) {
                    for (final PsiElement expression : conditions) {
                        if (expression instanceof BinaryExpression) {
                            final BinaryExpression instanceOfExpression = (BinaryExpression) expression;
                            if (instanceOfExpression.getOperationType() == PhpTokenTypes.kwINSTANCEOF) {
                                testSubject = instanceOfExpression.getLeftOperand();
                                break;
                            }
                        }
                    }
                }

                if (testSubject != null) {
                    for (final PsiElement expression : conditions) {
                        if (expression instanceof BinaryExpression) {
                            final BinaryExpression binaryExpression = (BinaryExpression) expression;
                            final PsiElement left                   = binaryExpression.getLeftOperand();
                            final PsiElement right                  = binaryExpression.getRightOperand();
                            if (
                                left != null && right != null &&
                                OpenapiTypesUtil.tsCOMPARE_EQUALITY_OPS.contains(binaryExpression.getOperationType())
                            ) {
                                if (
                                    OpenapiEquivalenceUtil.areEqual(testSubject, left) ||
                                    OpenapiEquivalenceUtil.areEqual(testSubject, right)
                                ) {
                                    holder.registerProblem(
                                            expression,
                                            MessagesPresentationUtil.prefixWithEa(messageInstanceOfComplementarity),
                                            ProblemHighlightType.WEAK_WARNING
                                    );
                                }
                            }
                        }
                    }
                }
            }

            private void inspectConditionsForDuplicatedCalls(@NotNull List<PsiElement> conditions) {
                if (conditions.size() < 2) {
                    return;
                }

                /* extract calls */
                final List<PsiElement> callsExtracted = new ArrayList<>();
                for (final PsiElement condition : conditions) {
                    if (condition instanceof BinaryExpression) {
                        final PsiElement left = ((BinaryExpression) condition).getLeftOperand();
                        if (left instanceof FunctionReference) {
                            callsExtracted.add(left);
                        }
                        final PsiElement right = ((BinaryExpression) condition).getRightOperand();
                        if (right instanceof FunctionReference) {
                            callsExtracted.add(right);
                        }
                    }
                }

                /* scan for duplicates */
                for (final PsiElement expression : callsExtracted) {
                    if (expression != null) {
                        /* put a stub */
                        callsExtracted.set(callsExtracted.indexOf(expression), null);
                        /* search duplicates in current scope */
                        for (final PsiElement innerLoopExpression : callsExtracted) {
                            if (innerLoopExpression != null && OpenapiEquivalenceUtil.areEqual(innerLoopExpression, expression)) {
                                holder.registerProblem(
                                        innerLoopExpression,
                                        MessagesPresentationUtil.prefixWithEa(messageDuplicateConditionPart)
                                );
                                callsExtracted.set(callsExtracted.indexOf(innerLoopExpression), null);
                            }
                        }
                    }
                }
            }

            private List<String> getPreviouslyModifiedVariables(@NotNull If ifStatement) {
                final List<String> result = new ArrayList<>();
                PsiElement previous = ifStatement.getPrevPsiSibling();
                while (previous != null) {
                    if (OpenapiTypesUtil.isStatementImpl(previous)) {
                        final PsiElement candidate = previous.getFirstChild();
                        if (OpenapiTypesUtil.isAssignment(candidate)) {
                            final PsiElement container = ((AssignmentExpression) candidate).getVariable();
                            if (container instanceof Variable) {
                                result.add(((Variable) container).getName());
                            }
                        }
                    }
                    previous = previous.getPrevSibling();
                }
                return result;
            }

            /**
             * @param objCondition to inspect
             */
            @Nullable
            private List<PsiElement> inspectExpressionsOrder(PsiElement objCondition, @Nullable IElementType[] arrOperationHolder) {
                final List<PsiElement> conditions = ExpressionSemanticUtil.getConditions(objCondition, arrOperationHolder);
                if (null == conditions) {
                    return null;
                }

                /* one item only, skip costs estimation */
                if (!SUGGEST_OPTIMIZING_CONDITIONS || conditions.size() < 2) {
                    return conditions;
                }

                /* verify if costs estimated are optimal */
                int intPreviousCost     = 0;
                PsiElement previousCond = null;
                for (final PsiElement condition : conditions) {
                    int intLoopCurrentCost = ExpressionCostEstimateUtil.getExpressionCost(condition, functionsSet);

                    if (
                        null != previousCond && intLoopCurrentCost < intPreviousCost &&
                        !ExpressionsCouplingCheckUtil.isSecondCoupledWithFirst(previousCond, condition)
                    ) {
                        holder.registerProblem(
                                condition,
                                MessagesPresentationUtil.prefixWithEa(messageOrdering),
                                ProblemHighlightType.WEAK_WARNING
                        );
                    }

                    intPreviousCost = intLoopCurrentCost;
                    previousCond = condition;
                }

                return conditions;
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Report instanceof usage flaws", REPORT_INSTANCE_OF_FLAWS, (isSelected) -> REPORT_INSTANCE_OF_FLAWS = isSelected);
            component.addCheckbox("Report literal and/or operators", REPORT_LITERAL_OPERATORS, (isSelected) -> REPORT_LITERAL_OPERATORS = isSelected);
            component.addCheckbox("Suggest optimizing conditions", SUGGEST_OPTIMIZING_CONDITIONS, (isSelected) -> SUGGEST_OPTIMIZING_CONDITIONS = isSelected);
        });
    }
}
