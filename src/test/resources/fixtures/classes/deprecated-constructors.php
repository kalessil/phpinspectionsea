<?php

class ClassWithDeprecatedConstructor
{
    public function <error descr="ClassWithDeprecatedConstructor has a deprecated constructor.">ClassWithDeprecatedConstructor</error>()
    {
    }
}

class ClassWithBakCompatibleConstructor
{
    public function __construct()
    {
    }

    public function ClassWithBakCompatibleConstructor()
    {
        $this->__construct();
    }
}


trait TraitWithNameMatchingMethod
{
    public function TraitWithNameMatchingMethod()
    {
    }
}

interface InterfaceWithNameMatchingMethod
{
    public function InterfaceWithNameMatchingMethod();
}