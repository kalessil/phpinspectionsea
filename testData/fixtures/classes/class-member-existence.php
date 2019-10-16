<?php

/**
 * @property $annotatedProperty
 * @method annotatedMethod
 */
abstract class Clazz {
    private $privateProperty;
    abstract function privateMethod();
}

function cases_holder_wrong_functions(Clazz $object) {
    return [
        <warning descr="[EA] This call seems to always return false, perhaps a wrong function being used.">method_exists($object, 'annotatedProperty')</warning>,
        <warning descr="[EA] This call seems to always return false, perhaps a wrong function being used.">method_exists($object, 'privateProperty')</warning>,
        method_exists($object, 'dynamicProperty'),

        <warning descr="[EA] This call seems to always return false, perhaps a wrong function being used.">property_exists($object, 'annotatedMethod')</warning>,
        <warning descr="[EA] This call seems to always return false, perhaps a wrong function being used.">property_exists($object, 'privateMethod')</warning>,
    ];
}

function cases_holder_always_true(Clazz $object) {
    return [
        property_exists($object, 'annotatedProperty'),
        <warning descr="[EA] This call seems to always return true, please check the workflow.">property_exists($object, 'privateProperty')</warning>,
        property_exists($object, 'dynamicProperty'),

        method_exists($object, 'annotatedMethod'),
        <warning descr="[EA] This call seems to always return true, please check the workflow.">method_exists($object, 'privateMethod')</warning>,
    ];
}