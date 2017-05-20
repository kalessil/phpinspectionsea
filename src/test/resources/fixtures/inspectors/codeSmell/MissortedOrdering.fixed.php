<?php

abstract class MyClass {
    // Alloweds.
    public function publicFunction() {}
    public static function publicStaticFunction() { }
    abstract public function abstractFunction();

    // Case #1
    public static function staticPublicFunction() { }

    // Case #2
    abstract public function publicAbstractFunction();
}
