<?php

class ClassWithValidMethods extends MissingClass {
    public function __wakeup() {
    }
}

class ClassWithStaticMethods extends MissingClass {
    public static function __wakeup() {
    }
}
class ClassWithPrivateMethods extends MissingClass {
    private function __wakeup() {
    }
}
class ClassWithParametrizedMethods extends MissingClass {
    private function __wakeup($optional = '') {
    }
}
class ClassWithReturningMethods extends MissingClass {
    private function __wakeup() {
        return '...';
    }
}