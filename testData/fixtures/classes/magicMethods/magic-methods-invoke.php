<?php

class ClassWithValidMethods extends MissingClass {
    public function __invoke() {
        return '...';
    }
}

class ClassWithStaticMethods extends MissingClass {
    public static function __invoke() {
    }
}
class ClassWithPrivateMethods extends MissingClass {
    private function __invoke() {
    }
}