<?php

class CasesHolder
{
    public function method() {}

    public static function staticTrigger()
    {
        static::method();
    }

    public function dynamicTrigger()
    {
        $this->method();
        $this->method();
        $this->method();
    }
}

$object = new CasesHolder();
$object->method();