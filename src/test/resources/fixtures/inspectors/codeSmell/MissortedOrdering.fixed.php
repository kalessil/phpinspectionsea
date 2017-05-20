<?php

abstract class MyClass {
    // Alloweds.
    function onlyFunction() {}
    public function publicFunction() {}
    public static function publicStaticFunction() { }
    abstract public function abstractFunction();
    abstract function abstractOnlyFunction();
    static function staticOnlyFunction() { }

    public
    static function publicStaticFunctionMultiline() { }

    // Should warn.
    public static function staticPublicFunction() { }

    abstract public function publicAbstractFunction();

    public static function staticPublicFunctionMultiline() { }
}
