<?php

class ClassWithDeprecatedConstructor
{
    public function __construct()
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