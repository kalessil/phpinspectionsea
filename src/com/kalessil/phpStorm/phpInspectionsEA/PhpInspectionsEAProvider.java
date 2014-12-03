package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.*;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.earlyReturns.NestedPositiveIfStatementsInspector;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.IfConditionalsWithoutCurvyBracketsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TernaryOperatorSimplifyInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnNecessaryDoubleQuotesInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TypeUnsafeComparisonInspector;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.MoreThanThreeArgumentsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.IfReturnReturnSimplificationInspector;

/*
===TO ADD===

public function __construct($kto, $blz, $options = array(), $messages = array()) - variable type to be array


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

                IfConditionalsWithoutCurvyBracketsInspector.class,

                NestedPositiveIfStatementsInspector.class,
                TernaryOperatorSimplifyInspector.class,
                IfReturnReturnSimplificationInspector.class,

                /*DefaultValuesForCallableParametersInspector.class,*/
                MoreThanThreeArgumentsInspector.class,
                dirnameCallOnFileConstantInspector.class
        };
    }
}
