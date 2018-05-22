<?php

class MethodsHolder {
    public static function method() {}

    public static function staticContext() {
        <error descr="$this is not accessible in static context"><warning descr="'self::method(...)' should be used instead.">$this</warning></error>->method();
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
