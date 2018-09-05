<?php

class MethodsHolder {
    public static function method() {}

    public static function staticContext() {
        // $this is not accessible in static context; Using $this when not in object context
        // $this->method();
    }

    public function nonStaticContext() {
        <warning descr="'self::method(...)' should be used instead.">$this</warning>->method();

        static::method();
        self::method();
    }
}

function cases_holder(MethodsHolder $one) {
    <warning descr="'...::method(...)' should be used instead.">$one->method()</warning>;
}
