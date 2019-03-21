<?php

class dynamicObjectsKeeper
{
    public function callMethod()
    {
    }

    public static function y()
    {
        static::callMethod();
    }

    public function z()
    {
        $this->callMethod();
    }

    public function p()
    {
        $this->callMethod();
    }
}

$o = new dynamicObjectsKeeper();
$o->z();