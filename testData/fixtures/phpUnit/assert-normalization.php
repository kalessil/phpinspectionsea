<?php

class TestCaseNormalization
{
    public function test()
    {
        <weak_warning descr="assertEquals would fit more here.">$this->assertTrue(false == $x)</weak_warning>;
        <weak_warning descr="assertSame would fit more here.">$this->assertNotFalse(false === $x)</weak_warning>;
        <weak_warning descr="assertNotEquals would fit more here.">$this->assertTrue(false != $x)</weak_warning>;
        <weak_warning descr="assertNotSame would fit more here.">$this->assertNotFalse(false !== $x)</weak_warning>;

        <weak_warning descr="assertNotEquals would fit more here.">$this->assertFalse(false == $x)</weak_warning>;
        <weak_warning descr="assertNotSame would fit more here.">$this->assertNotTrue(false === $x)</weak_warning>;
        <weak_warning descr="assertEquals would fit more here.">$this->assertFalse(false != $x)</weak_warning>;
        <weak_warning descr="assertSame would fit more here.">$this->assertNotTrue(false !== $x)</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="assertEquals would fit more here.">$this->assertTrue(false == $x, '')</weak_warning>;
        <weak_warning descr="assertSame would fit more here.">$this->assertNotFalse(false === $x, '')</weak_warning>;
        <weak_warning descr="assertNotEquals would fit more here.">$this->assertTrue(false != $x, '')</weak_warning>;
        <weak_warning descr="assertNotSame would fit more here.">$this->assertNotFalse(false !== $x, '')</weak_warning>;

        <weak_warning descr="assertNotEquals would fit more here.">$this->assertFalse(false == $x, '')</weak_warning>;
        <weak_warning descr="assertNotSame would fit more here.">$this->assertNotTrue(false === $x, '')</weak_warning>;
        <weak_warning descr="assertEquals would fit more here.">$this->assertFalse(false != $x, '')</weak_warning>;
        <weak_warning descr="assertSame would fit more here.">$this->assertNotTrue(false !== $x, '')</weak_warning>;
    }
}