package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.*;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.earlyReturns.NestedPositiveIfStatementsInspector;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.IfConditionalsWithoutGroupStatementInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TernaryOperatorSimplifyInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnNecessaryDoubleQuotesInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TypeUnsafeComparisonInspector;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.MoreThanThreeArgumentsInspector;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.IfReturnReturnSimplificationInspector;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces.ArrayTypeOfParameterByDefaultValueInspector;

/*
Some cases of interest:

unset(...);                     - sequential unset calls
unset(...);

for (.., ... count(...), ...)   - count to be called only once

foreach(...) {                  - code duplicates to be resolved
    <children to check: assignments, call-expressions and nothing more>
    <call-expressions without any parameters - eg getting env parameters>
    ...
    $container[<call-expression>]..[...]...[...] = <call expression>;
    ...
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

                IfConditionalsWithoutGroupStatementInspector.class,

                NestedPositiveIfStatementsInspector.class,
                TernaryOperatorSimplifyInspector.class,
                IfReturnReturnSimplificationInspector.class,

                /*DefaultValuesForCallableParametersInspector.class,*/
                ArrayTypeOfParameterByDefaultValueInspector.class,

                MoreThanThreeArgumentsInspector.class,
                dirnameCallOnFileConstantInspector.class
        };
    }
}
