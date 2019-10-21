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
    private static function <error descr="[EA] __callStatic must be public.">__callStatic</error>($name, $arguments) {
    }
}
class ClassWithParametersByReferenceMethods extends MissingClass {
    public static function <error descr="[EA] __callStatic cannot accept arguments by reference.">__callStatic</error>(&$name, $arguments) {
    }
}
class ClassWithWrongAmountOfParametersMethods extends MissingClass {
    public static function <error descr="[EA] __callStatic accepts exactly 2 arguments.">__callStatic</error>($name) {
    }
}
