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
===TO ADD===
if($changestatusqc = UserlogPeer::doSelectOne($c))                               - assignment in if/while

if/elseif(...)                                                                   - argument to be boolean

unset(...);
unset(...);

for (.., ... count(...), ...)

method_exists and other reflection workarounds instead of interfaces

import com.kalessil.phpstorm.PhpInspectionsEA.inspectors.codeSmell.DefaultValuesForCallableParametersInspector;
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
