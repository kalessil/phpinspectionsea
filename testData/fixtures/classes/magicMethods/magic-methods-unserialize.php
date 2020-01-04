<?php

class ClassWithValidMethods extends MissingClass {
    public function __unserialize($data) {
    }
}

class ClassWithStaticMethods extends MissingClass {
    public static function <error descr="[EA] __unserialize cannot be static.">__unserialize</error>($data) {
    }
}
class ClassWithPrivateMethods extends MissingClass {
    private function <error descr="[EA] __unserialize must be public.">__unserialize</error>($data) {
    }
}
class ClassWithMissingParametersMethods extends MissingClass {
    public function <error descr="[EA] __unserialize accepts exactly 1 arguments.">__unserialize</error>() {
    }
}
class ClassWithReturningMethods extends MissingClass {
    public function __unserialize($data) {
        <error descr="[EA] __unserialize cannot return a value.">return '...';</error>
    }
}