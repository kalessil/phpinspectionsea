<?php

class staticObjectsKeeper {
    public static function callMethod() {}

    public static function y() {
        <warning descr="'static::callMethod(...)' should be used instead"><error descr="$this is not accessible in static context">$this</error></warning>->callMethod();
    }

    public function z() {
        <warning descr="'static::callMethod(...)' should be used instead">$this</warning>->callMethod();
    }

    public function p() {
        static::callMethod();
    }
}

$o = new staticObjectsKeeper();
$o-><warning descr="'...::y(...)' should be used instead">y</warning>();