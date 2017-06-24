<?php

class TestCaseNormalization
{
    public function test()
    {
        <weak_warning descr="assertEquals should be used instead.">$this->assertTrue(false == $x)</weak_warning>;
        <weak_warning descr="assertSame should be used instead.">$this->assertNotFalse(false === $x)</weak_warning>;
        <weak_warning descr="assertNotEquals should be used instead.">$this->assertTrue(false != $x)</weak_warning>;
        <weak_warning descr="assertNotSame should be used instead.">$this->assertNotFalse(false !== $x)</weak_warning>;

        <weak_warning descr="assertNotEquals should be used instead.">$this->assertFalse(false == $x)</weak_warning>;
        <weak_warning descr="assertNotSame should be used instead.">$this->assertNotTrue(false === $x)</weak_warning>;
        <weak_warning descr="assertEquals should be used instead.">$this->assertFalse(false != $x)</weak_warning>;
        <weak_warning descr="assertSame should be used instead.">$this->assertNotTrue(false !== $x)</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="assertEquals should be used instead.">$this->assertTrue(false == $x, '')</weak_warning>;
        <weak_warning descr="assertSame should be used instead.">$this->assertNotFalse(false === $x, '')</weak_warning>;
        <weak_warning descr="assertNotEquals should be used instead.">$this->assertTrue(false != $x, '')</weak_warning>;
        <weak_warning descr="assertNotSame should be used instead.">$this->assertNotFalse(false !== $x, '')</weak_warning>;

        <weak_warning descr="assertNotEquals should be used instead.">$this->assertFalse(false == $x, '')</weak_warning>;
        <weak_warning descr="assertNotSame should be used instead.">$this->assertNotTrue(false === $x, '')</weak_warning>;
        <weak_warning descr="assertEquals should be used instead.">$this->assertFalse(false != $x, '')</weak_warning>;
        <weak_warning descr="assertSame should be used instead.">$this->assertNotTrue(false !== $x, '')</weak_warning>;
    }
}