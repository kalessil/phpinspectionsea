<?php

class SuperClass
{
    protected function __construct() {}

    public function doSomething1() {}

    public function doSomething2($x) {}

    public function doSomethingElse1($x, $y, $z) {}

    public function doSomethingElse2($x, $y, $z) {}

    public function withClassConstants($x = __CLASS__, $y = __METHOD__) {}
}

class ChildClass extends SuperClass
{
    public function __construct() {
        parent::__construct();
    }

    /** Some PhpDoc here */
    public function <weak_warning descr="'doSomething1' method can be dropped, as it only calls parent's one.">doSomething1</weak_warning>() {
        parent::doSomething1();
    }

    public function <weak_warning descr="'doSomething2' method can be dropped, as it only calls parent's one.">doSomething2</weak_warning>($x) {
        parent::doSomething2($x);
    }

    public function doSomethingElse1($x, $y, $z) {
        parent::doSomethingElse1($z, $y, $x);
    }

    public function doSomethingElse2($x, $y, $z) {
        parent::doSomethingElse1($x, $y, (int)$z);
    }

    public function withClassConstants($x = __CLASS__, $y = __METHOD__) {
        parent::withClassConstants($x, $y);
    }
}