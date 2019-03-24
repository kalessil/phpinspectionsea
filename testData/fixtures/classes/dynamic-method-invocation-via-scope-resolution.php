<?php

class CasesHolder
{
    public function method() {}

    public static function staticTrigger()
    {
        static::<error descr="'...->method(...)' should be used instead.">method</error>();
    }

    public function dynamicTrigger()
    {
        <weak_warning descr="'$this->method(...)' should be used instead.">static::method()</weak_warning>;
        <weak_warning descr="'$this->method(...)' should be used instead.">self::method()</weak_warning>;
        <weak_warning descr="'$this->method(...)' should be used instead.">CasesHolder::method()</weak_warning>;
    }
}

$object = new CasesHolder();
<error descr="'...->method(...)' should be used instead.">$object::method()</error>;