<?php

class ClassWithDeprecatedConstructor
{
    public function <error descr="ClassWithDeprecatedConstructor has a deprecated constructor">ClassWithDeprecatedConstructor</error>()
    {
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