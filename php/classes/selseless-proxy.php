<?php

class A
{
    protected function __construct()
    {
    }

    public function doSomething1()
    {
    }

    public function doSomething2($x)
    {
    }

    public function doSomethingElse1($x, $y, $z)
    {
    }

    public function doSomethingElse2($x, $y, $z)
    {
    }
}

class B extends A
{
    public function __construct()
    {
        parent::__construct();
    }

    public function doSomething1() // -> reported
    {
        parent::doSomething1();
    }

    public function doSomething2($x) // -> reported
    {
        parent::doSomething2($x);
    }

    public function doSomethingElse1($x, $y, $z)
    {
        parent::doSomethingElse1($z, $y, $x);
    }

    public function doSomethingElse2($x, $y, $z)
    {
        parent::doSomethingElse1($x, $y, (int)$z);
    }
}