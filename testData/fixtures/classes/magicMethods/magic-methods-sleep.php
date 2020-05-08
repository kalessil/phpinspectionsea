<?php

class ClassWithValidMethods_First extends MissingClass {
    public function __sleep() {
        return [ 'item' ];
    }
}
class ClassWithValidMethods_Second extends MissingClass {
    public function __sleep(): array {
        return [ 'item' ];
    }
}
class ClassWithValidMethods_Third extends MissingClass {
    /** @return string[] */
    public function __sleep(): array {
        return [ 'item' ];
    }
}

class ClassWithStaticMethods extends MissingClass {
    public static function <error descr="[EA] __sleep cannot be static.">__sleep</error>() {
        return [];
    }
}
class ClassWithPrivateMethods extends MissingClass {
    private function <error descr="[EA] __sleep must be public.">__sleep</error>() {
        return [];
    }
}
class ClassWithParametrizedMethods extends MissingClass {
    public function <error descr="[EA] __sleep cannot accept arguments.">__sleep</error>($optional = '...') {
        return [];
    }
}
class ClassWithWronglyReturningMethods extends MissingClass {
    public function __sleep() {
        <error descr="[EA] __sleep must return array (resolved: 'string').">return '...';</error>
    }
}