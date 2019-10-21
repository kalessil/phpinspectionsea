<?php

class ClassWithValidMethods extends MissingClass {
    public function __debugInfo() {
        return [];
    }
}

class ClassWithStaticMethods extends MissingClass {
    public static function __debugInfo() {
        return [];
    }
}
class ClassWithParametrizedMethods extends MissingClass {
    public function __debugInfo($optional = '...') {
        return [];
    }
}
class ClassWithPrivateMethods extends MissingClass {
    private function __debugInfo($optional = '...') {
        return [];
    }
}
class ClassWithWronglyReturningMethods extends MissingClass {
    private function __debugInfo($optional = '...') {
        return '...';
    }
}