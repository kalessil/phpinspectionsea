<?php

class ClassWithValidMethods extends MissingClass {
    public function __serialize() {
        return [];
    }
}

class ClassWithStaticMethods extends MissingClass {
    public static function <error descr="[EA] __serialize cannot be static.">__serialize</error>() {
        return [];
    }
}
class ClassWithPrivateMethods extends MissingClass {
    private function <error descr="[EA] __serialize must be public.">__serialize</error>() {
        return [];
    }
}
class ClassWithParametrizedMethods extends MissingClass {
    public function <error descr="[EA] __serialize cannot accept arguments.">__serialize</error>($optional = '...') {
        return [];
    }
}
class ClassWithWronglyReturningMethods extends MissingClass {
    public function __serialize() {
        <error descr="[EA] __serialize must return array.">return '...';</error>
    }
}