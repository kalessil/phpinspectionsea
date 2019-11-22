<?php

class ClassWithValidMethods extends MissingClass {
    public function __wakeup() {
    }
}

class ClassWithStaticMethods extends MissingClass {
    public static function <error descr="[EA] __wakeup cannot be static.">__wakeup</error>() {
    }
}
class ClassWithPrivateMethods extends MissingClass {
    private function __wakeup() {
    }
}
class ClassWithParametrizedMethods extends MissingClass {
    public function <error descr="[EA] __wakeup cannot accept arguments.">__wakeup</error>($optional = '') {
    }
}
class ClassWithReturningMethods extends MissingClass {
    public function __wakeup() {
        <error descr="[EA] __wakeup cannot return a value.">return '...';</error>
    }
}