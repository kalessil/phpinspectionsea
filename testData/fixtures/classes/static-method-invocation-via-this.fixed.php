<?php

class MethodsHolder {
    public static function method() {}

    public static function staticContext() {
        // $this is not accessible in static context; Using $this when not in object context
        // $this->method();
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
