package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.AmbiguousMethodsCallsInArrayMappingInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.MoreThanThreeArgumentsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.earlyReturns.NestedPositiveIfStatementsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.propel16.CountOnPropelCollectionInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.CallableParameterUseCaseInTypeContextInspection;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.ForeachSourceInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.LoopWhichDoesNotLoopInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OnlyWritesOnParameterInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.DefaultValueInElseBranchInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.IfReturnReturnSimplificationInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.NotOptimalIfConditionsInspection;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.StrlenInEmptyStringCheckContextInspection;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces.ArrayTypeOfParameterByDefaultValueInspector;

/*
===TODO===:

NotOptimalIfConditions
        - try resolving binary operations with constants and lookup types of both arguments
        - method/function references with bool result should be explicitly compared with bool
        - null comparison && instanceof: null comparison not needed (check if instanceof, take argument,
                find if used with !is_null, !== null - no sense or logical error)

ForeachSourceInspector
        foreach source modification within loop
        foreach ($source as $k => $v) {
                ...
                unset($source[$k]);
                ...
        }


===Cases of interest===

CallableParameterUseCaseInTypeContextInspection - but for return statements

condition: test if null and test type(s)/instance(s)of or test several types|instance(s)of

A extends B,C, B extends C - interface contracts or lack of structure knowledge?


===PHP5 transition===

Migration to Reflection API (ReflectionClass):
        constant, is_a, method_exists, property_exists, is_subclass_of are from PHP 4 world
        and not dealing with traits, annotations and so on. Mark deprecated.
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
                OnlyWritesOnParameterInspector.class,
                AmbiguousMemberInitializationInspector.class,
                LoopWhichDoesNotLoopInspector.class,

                TypesCastingWithFunctionsInspector.class,

                CountOnPropelCollectionInspector.class,
                ForeachSourceInspector.class,
                CallableParameterUseCaseInTypeContextInspection.class
        };
    }
}
