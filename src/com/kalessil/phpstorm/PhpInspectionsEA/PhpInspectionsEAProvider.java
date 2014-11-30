package com.kalessil.phpstorm.PhpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;

import com.kalessil.phpstorm.PhpInspectionsEA.inspectors.apiUsage.IsEmptyFunctionUsageInspector;
import com.kalessil.phpstorm.PhpInspectionsEA.inspectors.apiUsage.IsNullFunctionUsageInspector;
import com.kalessil.phpstorm.PhpInspectionsEA.inspectors.apiUsage.UnSafeIsSetOverArrayInspector;
import com.kalessil.phpstorm.PhpInspectionsEA.inspectors.languageConstructions.UnNecessaryDoubleQuotesInspector;

/*import com.kalessil.phpstorm.PhpInspectionsEA.inspectors.codeSmell.DefaultValuesForCallableParametersInspector;
import com.kalessil.phpstorm.PhpInspectionsEA.inspectors.codeSmell.MoreThanThreeArgumentsInspector;
import com.kalessil.phpstorm.PhpInspectionsEA.inspectors.earlyReturns.NestedPositiveIfStatementsInspector;
import com.kalessil.phpstorm.PhpInspectionsEA.inspectors.languageConstructions.TypeUnsafeComparisonInspector;
*/

public class PhpInspectionsEAProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        return new Class[]{
                IsNullFunctionUsageInspector.class,
                IsEmptyFunctionUsageInspector.class,
                UnSafeIsSetOverArrayInspector.class,

                UnNecessaryDoubleQuotesInspector.class/*,
                TypeUnsafeComparisonInspector.class,

                NestedPositiveIfStatementsInspector.class,

                DefaultValuesForCallableParametersInspector.class,
                MoreThanThreeArgumentsInspector.class*/
        };
    }
}
