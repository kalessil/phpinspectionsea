package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.ForgottenDebugOutputInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsEmptyFunctionUsageInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.UnSafeIsSetOverArrayInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsNullFunctionUsageInspector;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.earlyReturns.NestedPositiveIfStatementsInspector;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.IfConditionalsWithoutCurvyBracketsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TernaryOperatorSimplifyInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnNecessaryDoubleQuotesInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TypeUnsafeComparisonInspector;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.MoreThanThreeArgumentsInspector;

/*
===TO ADD===

public function __construct($kto, $blz, $options = array(), $messages = array()) - variable type to be array


if($changestatusqc = UserlogPeer::doSelectOne($c))                               - assignment in if/while


if/elseif(...)                                                                   - argument to be boolean


if(...)
    return true;
return false;


sizeof(array?)


date('H:i:s') - any pre-defined dates formats?


===TO FIX===
\n\r\t or single quotes in double quotes inspection
'else if' in conditionals


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

                IfConditionalsWithoutCurvyBracketsInspector.class,
                NestedPositiveIfStatementsInspector.class,
                TernaryOperatorSimplifyInspector.class,

                /*DefaultValuesForCallableParametersInspector.class,*/
                MoreThanThreeArgumentsInspector.class
        };
    }
}
