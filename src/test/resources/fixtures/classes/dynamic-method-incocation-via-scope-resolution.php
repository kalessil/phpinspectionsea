<?php

class dynamicObjectsKeeper
{
    public function callMethod()
    {
    }

    public static function y()
    {
        static::<error descr="'...->callMethod(...)' should be used instead.">callMethod</error>();
    }

    public function z()
    {
        <weak_warning descr="'$this->callMethod(...)' should be used instead.">static::callMethod()</weak_warning>;
    }

    public function p()
    {
        <weak_warning descr="'$this->callMethod(...)' should be used instead.">self::callMethod()</weak_warning>;
    }
}

$o = new dynamicObjectsKeeper();
<error descr="'...->z(...)' should be used instead.">$o::z()</error>;