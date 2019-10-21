<?php

class ClassWithValidMethods extends MissingClass {
    public static function __callStatic($name, $arguments) {
    }
}

class ClassWithNonStaticMethods extends MissingClass {
    public function <error descr="[EA] __callStatic must be static.">__callStatic</error>($name, $arguments) {
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
