<?php

class TestAssertInstanceof
{
    public function test()
    {
        <weak_warning descr="'assertInstanceOf(...)' should be used instead.">$this->assertTrue($x instanceof \stdClass)</weak_warning>;
        <weak_warning descr="'assertInstanceOf(...)' should be used instead.">$this->assertNotFalse($x instanceof \stdClass)</weak_warning>;
        <weak_warning descr="'assertNotInstanceOf(...)' should be used instead.">$this->assertFalse($x instanceof \stdClass)</weak_warning>;
        <weak_warning descr="'assertNotInstanceOf(...)' should be used instead.">$this->assertNotTrue($x instanceof \stdClass)</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="'assertInstanceOf(...)' should be used instead.">$this->assertTrue($x instanceof \stdClass, '')</weak_warning>;
        <weak_warning descr="'assertInstanceOf(...)' should be used instead.">$this->assertNotFalse($x instanceof \stdClass, '')</weak_warning>;
        <weak_warning descr="'assertNotInstanceOf(...)' should be used instead.">$this->assertFalse($x instanceof \stdClass, '')</weak_warning>;
        <weak_warning descr="'assertNotInstanceOf(...)' should be used instead.">$this->assertNotTrue($x instanceof \stdClass, '')</weak_warning>;
    }
}