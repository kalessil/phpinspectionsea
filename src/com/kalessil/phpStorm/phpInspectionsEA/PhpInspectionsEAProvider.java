package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;

/*

Pre-collected release notes:

Added shortcuts description

ArrayCastingEquivalentInspector: message updated to warn about side-effects
UnSafeIsSetOverArrayInspector: fixed warning text regarding null comparison
CallableParameterUseCaseInTypeContextInspection: assignment type violation message updated

ForeachSourceInspector: fixed \Traversable detection
AlterInForeachInspector: added checks for unset on reference
UnSafeIsSetOverArrayInspector: do not warn if context is return/assignment statement
NotOptimalIfConditionsInspection - resolved false-positive with assignment expression

ForeachOnArrayComponentsInspector: added


===TODO===:

octdec -> intval(..., 8);
    - judge as an alias

$cookies[count($cookies) - 1]
    - replacement is 'end(...)', but it changes internal pointer in array, so can introduce side-effects in loops
    - legal in unset context (1 ... n parameters)

AdditionOperationOnArraysInspection:
    - re-implement to check any of binary/mathematical operations has been applied on an array

===POOL===

current(array_keys(...)) => key(...)

StaticInvocationViaThisInspector:
    - static calls on any objects, not only this (may be quite heavy due to index lookup)

Empty functions/methods:
    - stubs, design issues

Empty try/catch
    - bad code, like no scream

'For' loops:
    use foreach instead

Magic numbers:
    needs additional research here

Confusing construct: BO ? bool|BO : BO|bool

PHP 5 migration: reflection API usage (ReflectionClass):
        constant, is_a, method_exists, property_exists, is_subclass_of are from PHP 4 world
        and not dealing with traits, annotations and so on. Mark deprecated.

*/
public class PhpInspectionsEAProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        return new Class[]{};
    }
}
