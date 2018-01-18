<?php

class TestAssertInternalType
{
    public function test()
    {
        <weak_warning descr="'assertNotInternalType('array', ...)' would fit more here.">$this->assertNotTrue(is_array([]))</weak_warning>;
        <weak_warning descr="'assertNotInternalType('scalar', ...)' would fit more here.">$this->assertFalse(is_scalar(''))</weak_warning>;
        <weak_warning descr="'assertInternalType('array', ...)' would fit more here.">$this->assertTrue(is_array([]))</weak_warning>;
        <weak_warning descr="'assertInternalType('scalar', ...)' would fit more here.">$this->assertNotFalse(is_scalar(''))</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="'assertNotInternalType('array', ...)' would fit more here.">$this->assertNotTrue(is_array([]), '')</weak_warning>;
        <weak_warning descr="'assertNotInternalType('scalar', ...)' would fit more here.">$this->assertFalse(is_scalar(''), '')</weak_warning>;
        <weak_warning descr="'assertInternalType('array', ...)' would fit more here.">$this->assertTrue(is_array([]), '')</weak_warning>;
        <weak_warning descr="'assertInternalType('scalar', ...)' would fit more here.">$this->assertNotFalse(is_scalar(''), '')</weak_warning>;
    }
}