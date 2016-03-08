<?php

class dynamicObjectsKeeper
{
    public function callMethod()
    {
    }

    public static function y()
    {
        static::callMethod(); // <- reported
    }

    public function z()
    {
        static::callMethod(); // <- reported
    }

    public function p()
    {
        self::callMethod(); // <- reported
    }
}

$o = new dynamicObjectsKeeper();
$o::z(); // <- reported