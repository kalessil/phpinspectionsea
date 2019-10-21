<?php

class ClassWithValidMethods extends MissingClass {
    public function __sleep() {
        return [];
    }
}

class ClassWithStaticMethods extends MissingClass {
    public static function __sleep() {
        return [];
    }
}
class ClassWithPrivateMethods extends MissingClass {
    private function __sleep() {
        return [];
    }
}
class ClassWithParametrizedMethods extends MissingClass {
    private function __sleep($optional = '...') {
        return [];
    }
}
class ClassWithWronglyReturningMethods extends MissingClass {
    private function __sleep() {
        return '...';
    }
}