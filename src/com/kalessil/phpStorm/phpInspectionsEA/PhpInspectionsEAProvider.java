package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;

/*
===Release notes===

===TODO===:

StrlenInEmptyStringCheckContextInspection: some patterns are not recognized
    if (strlen(...))        - if
    if (strlen(...) || ...) - binary expression
    !strlen(...)            - unary expression


NotOptimalIfConditionsInspection (increment to 1.2.0):
    dedicate all comparisons to separate inspection, specialized in logical bugs.
    e.g. null/instanceof combination.

===POOL===

Regex semantics lookup
    [\x]          => \x
    [0-9]         => \d
    [seg][seq]... => [seq]{N}
    [seg][seq]+   => [seq]{2,}
    [seg][seq]*   => [seq]+
    [seg][seq]?   => [seq]{1,2}
    [:class:]     => \x
    /^text/       => strpos === 0
    /text/        => strpos !== false


$cookies[count($cookies) - 1]
    - replacement is 'end(...)', but it changes internal pointer in array, so can introduce side-effects in loops
    - legal in unset context (1 ... n parameters)

ctype_alnum|ctype_alpha vs regular expressions test
    - challenge is polymorphic pattern recognition

current(array_keys(...))
    => key(), rare case

AdditionOperationOnArraysInspection:
    - re-implement to check any of binary/mathematical operations has been applied on an array

StaticInvocationViaThisInspector:
    - static calls on any objects, not only this (may be quite heavy due to index lookup)

Empty functions/methods:
    - stubs, design issues

Empty try/catch
    - bad code, like no scream

'For' loops, array_walk with closure:
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
