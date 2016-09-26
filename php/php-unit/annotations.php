<?php

class theOneToCover
{
    /**
     * @test
     */
    public function testAmbigousTag()
    {
    }

    public function doSomething()
    {
    }
}

class theOneToCoverTest
{
    /**  */
    public function init() // not reported (once was reported as not a test)
    {
    }

    /**
     * @depends init
     */
    public function testCorrectDepends()
    {

    }

    /**
     * @depends something
     */
    public function testIncorrectDepends() // <-reported
    {

    }

    /**
     * @covers \theOneToCover::doSomething
     */
    public function testCorrectCovers()
    {

    }

    /**
     * @covers \theOneToCover::doSomethingElse
     */
    public function testIncorrectCovers1() // <-reported
    {

    }

    /**
     * @covers \Something::doSomething
     */
    public function testIncorrectCovers2() // <-reported
    {

    }
}