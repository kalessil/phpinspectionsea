<?php

class TestAssertInstanceof
{
    public function test()
    {
        <weak_warning descr="'assertInstanceOf(...)' would fit more here.">$this->assertTrue($x instanceof \stdClass)</weak_warning>;
        <weak_warning descr="'assertInstanceOf(...)' would fit more here.">$this->assertNotFalse($x instanceof \stdClass)</weak_warning>;
        <weak_warning descr="'assertNotInstanceOf(...)' would fit more here.">$this->assertFalse($x instanceof \stdClass)</weak_warning>;
        <weak_warning descr="'assertNotInstanceOf(...)' would fit more here.">$this->assertNotTrue($x instanceof \stdClass)</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="'assertInstanceOf(...)' would fit more here.">$this->assertTrue($x instanceof \stdClass, '')</weak_warning>;
        <weak_warning descr="'assertInstanceOf(...)' would fit more here.">$this->assertNotFalse($x instanceof \stdClass, '')</weak_warning>;
        <weak_warning descr="'assertNotInstanceOf(...)' would fit more here.">$this->assertFalse($x instanceof \stdClass, '')</weak_warning>;
        <weak_warning descr="'assertNotInstanceOf(...)' would fit more here.">$this->assertNotTrue($x instanceof \stdClass, '')</weak_warning>;
    }
}