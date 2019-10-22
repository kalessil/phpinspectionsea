<?php

class ClassWithValidMethods extends MissingClass {
    public function __invoke() {
        return '...';
    }
}

class ClassWithStaticMethods extends MissingClass {
    public static function <error descr="[EA] __invoke cannot be static.">__invoke</error>() {
    }
}
class ClassWithPrivateMethods extends MissingClass {
    private function <error descr="[EA] __invoke must be public.">__invoke</error>() {
    }
}