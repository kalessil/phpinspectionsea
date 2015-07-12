<?php

class A
{
    public function doSomething()
    {
    }
}

class B extends A
{
    public function doSomething()
    {
        parent::doSomething();
    }
}