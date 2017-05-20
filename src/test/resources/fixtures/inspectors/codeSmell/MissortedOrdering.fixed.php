<?php

interface MyInterface
{
    public static function publicStaticFunction();

    public static function staticPublicFunction();
}

abstract class MyClass {
    // Alloweds.
    function onlyFunction() {}
    public function publicFunction() {}
    public static function publicStaticFunction() { }
    abstract public function abstractFunction();
    abstract function abstractOnlyFunction();
    static function staticOnlyFunction() { }
    abstract public static function abstractPublicStaticFunction();

    public
    static function publicStaticFunctionMultiline() { }

    // Should warn.
    public static function staticPublicFunction() { }

    abstract public function publicAbstractFunction();

    public static function staticPublicFunctionMultiline() { }

    abstract public static function abstractStaticPublicFunction();

    abstract public static function staticAbstractPublicFunction();

    abstract public static function staticPublicAbstractFunction();
}
