<?php

class x {
    static protected $property;
    static function callMethod() {}

    static function y() {
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