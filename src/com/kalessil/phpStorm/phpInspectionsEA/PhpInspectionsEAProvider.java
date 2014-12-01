package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsEmptyFunctionUsageInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.UnSafeIsSetOverArrayInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsNullFunctionUsageInspector;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnNecessaryDoubleQuotesInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TypeUnsafeComparisonInspector;

import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.MoreThanThreeArgumentsInspector;

/*
import com.kalessil.phpstorm.PhpInspectionsEA.inspectors.codeSmell.DefaultValuesForCallableParametersInspector;
import com.kalessil.phpstorm.PhpInspectionsEA.inspectors.earlyReturns.NestedPositiveIfStatementsInspector;
*/
public class PhpInspectionsEAProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        return new Class[]{
                IsNullFunctionUsageInspector.class,
                IsEmptyFunctionUsageInspector.class,
                UnSafeIsSetOverArrayInspector.class,

                UnNecessaryDoubleQuotesInspector.class,
                TypeUnsafeComparisonInspector.class,

                /*NestedPositiveIfStatementsInspector.class,

                DefaultValuesForCallableParametersInspector.class,*/
                MoreThanThreeArgumentsInspector.class
        };
    }
}
