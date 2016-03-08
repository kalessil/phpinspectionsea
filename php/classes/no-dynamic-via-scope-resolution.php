<?php

class dynamicObjectsKeeper {
    public function callMethod() {}

    public static function y() {
        static::callMethod(); // <- reported
    }

    public function z() {
        static::callMethod();
    }

    public function p() {
        self::callMethod();
    }
}

$o = new dynamicObjectsKeeper();
$o::z(); // <- reported