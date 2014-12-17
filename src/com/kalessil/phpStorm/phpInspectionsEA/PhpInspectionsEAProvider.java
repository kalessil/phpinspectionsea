package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.AmbiguousMethodsCallsInArrayMappingInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.MoreThanThreeArgumentsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.earlyReturns.NestedPositiveIfStatementsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OnlyWritesOnParameterInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.DefaultValueInElseBranchInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.IfReturnReturnSimplificationInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.NotOptimalIfConditionsInspection;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.StrlenInEmptyStringCheckContextInspection;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces.ArrayTypeOfParameterByDefaultValueInspector;

/*
===Bugs===:

OnlyWritesOnParameterInspector:
    - think of applying on foreach
DefaultValueInElseBranchInspector
    - false positives, when assignment is concatenation or array with calls inside

Argument for @NotNull parameter 'psiElement' of com/intellij/codeInspection/ProblemsHolder.registerProblem must not be null
java.lang.IllegalArgumentException: Argument for @NotNull parameter 'psiElement' of com/intellij/codeInspection/ProblemsHolder.registerProblem must not be null
	at com.intellij.codeInspection.ProblemsHolder.registerProblem(ProblemsHolder.java)
	at com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.NotOptimalIfConditionsInspection$1.analyseBinaryExpression(NotOptimalIfConditionsInspection.java:95)
	at com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.NotOptimalIfConditionsInspection$1.inspectExpression(NotOptimalIfConditionsInspection.java:63)
	at com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.NotOptimalIfConditionsInspection$1.visitPhpIf(NotOptimalIfConditionsInspection.java:34)


===Some cases of interest===:

-- review open api
PhpCodeInsightUtil.isEmptyBody

-- not needed initialization
var $member = null;

-- loop statement that doesn't loop
foreach (...) {
    throw|break|return;
}

-- suspicious counting in foreach
foreach(... as ...) {
    ...
    $variable++||++$variable;
}

*/
public class PhpInspectionsEAProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        return new Class[]{
                IsNullFunctionUsageInspector.class,
                IsEmptyFunctionUsageInspector.class,
                UnSafeIsSetOverArrayInspector.class,
                ForgottenDebugOutputInspector.class,

                UnNecessaryDoubleQuotesInspector.class,
                TypeUnsafeComparisonInspector.class,
                TypeUnsafeArraySearchInspector.class,

                IfConditionalsWithoutGroupStatementInspector.class,

                NestedPositiveIfStatementsInspector.class,
                TernaryOperatorSimplifyInspector.class,
                IfReturnReturnSimplificationInspector.class,
                /*IfExpressionInEarlyReturnContextInspector.class,*/
                DefaultValueInElseBranchInspector.class,

                /*DefaultValuesForCallableParametersInspector.class,*/
                ArrayTypeOfParameterByDefaultValueInspector.class,
                SenselessCommaInArrayDefinitionInspector.class,

                MoreThanThreeArgumentsInspector.class,
                dirnameCallOnFileConstantInspector.class,
                AmbiguousMethodsCallsInArrayMappingInspector.class,
                CountInSecondIterateExpressionInspector.class,
                SequentialUnSetCallsInspector.class,


                NotOptimalIfConditionsInspection.class,
                StrlenInEmptyStringCheckContextInspection.class,
                OnlyWritesOnParameterInspector.class
        };
    }
}
