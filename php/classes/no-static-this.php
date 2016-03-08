<?php

class staticObjectsKeeper {
    public static function callMethod() {}

    public static function y() {
        $this->callMethod(); // <- reported
    }

    public function z() {
        $this->callMethod(); // <- reported
    }

    public function p() {
        static::callMethod();
    }
}

$o = new staticObjectsKeeper();
$o->y(); // <- reported