<?php

class theOneToCover
{
    public function doSomething()
    {
    }
}

class testClass
{
    public function init()
    {
    }

    /**
     * @depends init
     */
    public function correctDepends()
    {

    }

    /**
     * @depends something
     */
    public function incorrectDepends() // <-reported
    {

    }

    /**
     * @covers \theOneToCover::doSomething
     */
    public function correctCovers()
    {

    }

    /**
     * @covers \theOneToCover::doSomethingElse
     */
    public function incorrectCovers1() // <-reported
    {

    }

    /**
     * @covers \Something::doSomething
     */
    public function incorrectCovers2() // <-reported
    {

    }
}