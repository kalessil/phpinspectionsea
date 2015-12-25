<?php

class x {
    static protected $property;
    public static function callMethod() {}

    public static function y() {
        $this->callMethod();
        return $this->property;
    }

    public function z() {
        $this->callMethod();
        return $this->property;
    }

    public function p() {
        static::callMethod();
        return static::$property;
    }
}

$o = new x();
$o->y();