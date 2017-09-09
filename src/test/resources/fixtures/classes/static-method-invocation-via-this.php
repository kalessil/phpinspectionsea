<?php

class staticObjectsKeeper {
    public static function callMethod() {}

    public static function y() {
        <error descr="$this is not accessible in static context"><warning descr="'static::callMethod(...)' should be used instead.">$this</warning></error>->callMethod();
    }

    public function z() {
        <warning descr="'static::callMethod(...)' should be used instead.">$this</warning>->callMethod();
    }

    public function p() {
        static::callMethod();
    }
}

$o = new staticObjectsKeeper();
<warning descr="'...::y(...)' should be used instead.">$o->y()</warning>;