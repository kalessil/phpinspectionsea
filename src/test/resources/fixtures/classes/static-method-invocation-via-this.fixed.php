<?php

class MethodsHolder {
    public static function method() {}

    public static function staticContext() {
        self::method();
    }

    public function nonStaticContext() {
        self::method();

        static::method();
        self::method();
    }
}

function cases_holder(MethodsHolder $one) {
    $one->method();
}
