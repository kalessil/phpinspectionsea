<?php

class ClassWithValidMethods extends MissingClass {
    public static function __callStatic($name, $arguments) {
    }
}

class ClassWithNonStaticMethods extends MissingClass {
    public function __callStatic($name, $arguments) {
    }
}
class ClassWithNonPrivateMethods extends MissingClass {
    private static function __callStatic($name, $arguments) {
    }
}
class ClassWithParametersByReferenceMethods extends MissingClass {
    private static function __callStatic(&$name, $arguments) {
    }
}
class ClassWithWrongAmountOfParametersMethods extends MissingClass {
    private static function __callStatic($name) {
    }
}
