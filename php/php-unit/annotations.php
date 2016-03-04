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
    public function init() // <- reported as not a UT
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