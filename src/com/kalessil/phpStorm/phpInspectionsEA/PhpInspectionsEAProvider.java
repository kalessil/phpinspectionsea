package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.AmbiguousMethodsCallsInArrayMappingInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.MoreThanThreeArgumentsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.earlyReturns.NestedPositiveIfStatementsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.propel16.CountOnPropelCollectionInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces.ArrayTypeOfParameterByDefaultValueInspector;

/*
===TODO===:

Performance: '(array) ...' equivalent construction
        - is_array($...) ? $... : array($...)

Probable bugs: Class overrides field of superclass

NotOptimalIfConditions
        - '... instanceof ...'|'is_*(...)' vs 'is_null(...)'|'=== null'
        - ... && [(] ... || ... [)], ... || [(] ... && ... [)] - parenthesis brackets are missing

Architecture: Class re-implements interface of superclass

Architecture: singleton inspection
    - private constructor and public getInstance - both should be presented


===POOL===

Architecture: private getters/setters

Architecture: return core type/class and boolean - preferred to return null instead

Architecture: CallableParameterUseCaseInTypeContextInspection, but for return statements

Confusing construct: BO ? bool|BO : BO|bool

Confusing construct: Unnecessary 'return/continue' statement

Confusing construct: IfReturnReturnSimplificationInspector:
        ifReturnElseReturn
        ifReturn[,ElseIfReturn]{1,}ElseReturn - possibly some branches can be merged

PHP 5 migration: reflection API usage (ReflectionClass):
        constant, is_a, method_exists, property_exists, is_subclass_of are from PHP 4 world
        and not dealing with traits, annotations and so on. Mark deprecated.

Hacks: is_array($) => (array)$ === $ - promised to be a performance improvement

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
                DirnameCallOnFileConstantInspector.class,
                AmbiguousMethodsCallsInArrayMappingInspector.class,
                CountInSecondIterateExpressionInspector.class,
                SequentialUnSetCallsInspector.class,

                NotOptimalIfConditionsInspection.class,
                StrlenInEmptyStringCheckContextInspection.class,
                OnlyWritesOnParameterInspector.class,
                AmbiguousMemberInitializationInspector.class,
                LoopWhichDoesNotLoopInspector.class,

                TypesCastingWithFunctionsInspector.class,
                ElvisOperatorCanBeUsedInspector.class,
                SenselessTernaryOperatorInspector.class,
                AdditionOperationOnArraysInspection.class,

                CountOnPropelCollectionInspector.class,
                ForeachSourceInspector.class,
                CallableParameterUseCaseInTypeContextInspection.class,
                MissingParentConstructorCallInspector.class,
                ClassMethodNameMatchesFieldNameInspector.class
        };
    }
}
