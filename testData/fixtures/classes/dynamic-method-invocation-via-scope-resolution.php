<?php

class CasesHolder
{
    public function method() {}

    public static function staticTrigger()
    {
        <warning descr="'...->method(...)' should be used instead.">static::method()</warning>;
    }

    public function dynamicTrigger()
    {
        <warning descr="'$this->method(...)' should be used instead.">static::method()</warning>;
        <warning descr="'$this->method(...)' should be used instead.">self::method()</warning>;
        <warning descr="'$this->method(...)' should be used instead.">CasesHolder::method()</warning>;
    }
}

$object = new CasesHolder();
<warning descr="'...->method(...)' should be used instead.">$object::method()</warning>;