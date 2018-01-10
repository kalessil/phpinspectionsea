<?php

class TestAssertInternalType
{
    public function test()
    {
        <weak_warning descr="'assertNotInternalType('array', ...)' should be used instead.">$this->assertNotTrue(is_array([]))</weak_warning>;
        <weak_warning descr="'assertNotInternalType('scalar', ...)' should be used instead.">$this->assertFalse(is_scalar(''))</weak_warning>;
        <weak_warning descr="'assertInternalType('array', ...)' should be used instead.">$this->assertTrue(is_array([]))</weak_warning>;
        <weak_warning descr="'assertInternalType('scalar', ...)' should be used instead.">$this->assertNotFalse(is_scalar(''))</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="'assertNotInternalType('array', ...)' should be used instead.">$this->assertNotTrue(is_array([]), '')</weak_warning>;
        <weak_warning descr="'assertNotInternalType('scalar', ...)' should be used instead.">$this->assertFalse(is_scalar(''), '')</weak_warning>;
        <weak_warning descr="'assertInternalType('array', ...)' should be used instead.">$this->assertTrue(is_array([]), '')</weak_warning>;
        <weak_warning descr="'assertInternalType('scalar', ...)' should be used instead.">$this->assertNotFalse(is_scalar(''), '')</weak_warning>;
    }
}