package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionToolProvider;

/*

===TODO===:

$this->staticMemberOrFunction call
        shall be static::staticMemberOrFunction

AmbiguousMethodsCallsInArrayMappingInspector:
        foreach ($content['multifiles'] as $multifile) {
            $found[] = $multifile;
        }

AdditionOperationOnArraysInspection:
        - re-implement to check any of binary/mathematical operations has been applied on an array

SlowArrayOperationsInLoopInspector:
        - more functions with O(n) complexity, e.g. array_unique

===POOL===

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
