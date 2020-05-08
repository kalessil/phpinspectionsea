<?php

class ClassWithValidMethods extends MissingClass {
    public function __debugInfo() {
        return [];
    }
}

class ClassWithStaticMethods extends MissingClass {
    public static function <error descr="[EA] __debugInfo cannot be static.">__debugInfo</error>() {
        return [];
    }
}
class ClassWithParametrizedMethods extends MissingClass {
    public function <error descr="[EA] __debugInfo cannot accept arguments.">__debugInfo</error>($optional = '...') {
        return [];
    }
}
class ClassWithPrivateMethods extends MissingClass {
    private function <error descr="[EA] __debugInfo must be public.">__debugInfo</error>() {
        return [];
    }
}
class ClassWithWronglyReturningMethods extends MissingClass {
    public function __debugInfo() {
        <error descr="[EA] __debugInfo must return array|null (resolved: 'string').">return '...';</error>
    }
}