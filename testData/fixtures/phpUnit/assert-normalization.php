<?php

class TestCaseNormalization
{
    public function test()
    {
        <weak_warning descr="[EA] assertEquals would fit more here.">$this->assertTrue(false == $x)</weak_warning>;
        <weak_warning descr="[EA] assertSame would fit more here.">$this->assertNotFalse(false === $x)</weak_warning>;
        <weak_warning descr="[EA] assertNotEquals would fit more here.">$this->assertTrue(false != $x)</weak_warning>;
        <weak_warning descr="[EA] assertNotSame would fit more here.">$this->assertNotFalse(false !== $x)</weak_warning>;

        <weak_warning descr="[EA] assertNotEquals would fit more here.">$this->assertFalse(false == $x)</weak_warning>;
        <weak_warning descr="[EA] assertNotSame would fit more here.">$this->assertNotTrue(false === $x)</weak_warning>;
        <weak_warning descr="[EA] assertEquals would fit more here.">$this->assertFalse(false != $x)</weak_warning>;
        <weak_warning descr="[EA] assertSame would fit more here.">$this->assertNotTrue(false !== $x)</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="[EA] assertEquals would fit more here.">$this->assertTrue(false == $x, '')</weak_warning>;
        <weak_warning descr="[EA] assertSame would fit more here.">$this->assertNotFalse(false === $x, '')</weak_warning>;
        <weak_warning descr="[EA] assertNotEquals would fit more here.">$this->assertTrue(false != $x, '')</weak_warning>;
        <weak_warning descr="[EA] assertNotSame would fit more here.">$this->assertNotFalse(false !== $x, '')</weak_warning>;

        <weak_warning descr="[EA] assertNotEquals would fit more here.">$this->assertFalse(false == $x, '')</weak_warning>;
        <weak_warning descr="[EA] assertNotSame would fit more here.">$this->assertNotTrue(false === $x, '')</weak_warning>;
        <weak_warning descr="[EA] assertEquals would fit more here.">$this->assertFalse(false != $x, '')</weak_warning>;
        <weak_warning descr="[EA] assertSame would fit more here.">$this->assertNotTrue(false !== $x, '')</weak_warning>;
    }
}