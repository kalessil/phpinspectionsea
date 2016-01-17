<?php

class A
{
    private function __construct()
    {
    }

    public function doSomething()
    {
    }
}

class B extends A
{
    public function __construct()
    {
        parent::__construct();
    }

    public function doSomething() // -> reported
    {
        parent::doSomething();
    }
}