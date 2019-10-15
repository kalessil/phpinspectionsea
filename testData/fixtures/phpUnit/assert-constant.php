<?php

class TestCaseAssertConstant
{
    public function testNormalization()
    {
        <weak_warning descr="[EA] 'assertNotTrue(...)' would fit more here.">$this->assertTrue(!$s)</weak_warning>;
        <weak_warning descr="[EA] 'assertNotFalse(...)' would fit more here.">$this->assertFalse(!$y)</weak_warning>;
    }

    public function testNormalizationWithMessage()
    {
        <weak_warning descr="[EA] 'assertNotTrue(...)' would fit more here.">$this->assertTrue(!$s, '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotFalse(...)' would fit more here.">$this->assertFalse(!$y, '')</weak_warning>;
    }

    public function test()
    {
        <weak_warning descr="[EA] 'assertNull(...)' would fit more here.">$this->assertSame(1, null)</weak_warning>;
        <weak_warning descr="[EA] 'assertNull(...)' would fit more here.">$this->assertSame(null, 1)</weak_warning>;
        <weak_warning descr="[EA] 'assertNotNull(...)' would fit more here.">$this->assertNotSame(1, null)</weak_warning>;
        <weak_warning descr="[EA] 'assertNotNull(...)' would fit more here.">$this->assertNotSame(null, 1)</weak_warning>;

        <weak_warning descr="[EA] 'assertTrue(...)' would fit more here.">$this->assertSame(true, 0)</weak_warning>;
        <weak_warning descr="[EA] 'assertNotTrue(...)' would fit more here.">$this->assertNotSame(true, 0)</weak_warning>;
        <weak_warning descr="[EA] 'assertTrue(...)' would fit more here.">$this->assertSame(0, true)</weak_warning>;
        <weak_warning descr="[EA] 'assertNotTrue(...)' would fit more here.">$this->assertNotSame(0, true)</weak_warning>;

        <weak_warning descr="[EA] 'assertFalse(...)' would fit more here.">$this->assertSame(false, 0)</weak_warning>;
        <weak_warning descr="[EA] 'assertNotFalse(...)' would fit more here.">$this->assertNotSame(false, 0)</weak_warning>;
        <weak_warning descr="[EA] 'assertFalse(...)' would fit more here.">$this->assertSame(0, false)</weak_warning>;
        <weak_warning descr="[EA] 'assertNotFalse(...)' would fit more here.">$this->assertNotSame(0, false)</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="[EA] 'assertNull(...)' would fit more here.">$this->assertSame(1, null, '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNull(...)' would fit more here.">$this->assertSame(null, 1, '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotNull(...)' would fit more here.">$this->assertNotSame(1, null, '')</weak_warning> ;
        <weak_warning descr="[EA] 'assertNotNull(...)' would fit more here.">$this->assertNotSame(null, 1, '')</weak_warning>;

        <weak_warning descr="[EA] 'assertTrue(...)' would fit more here.">$this->assertSame(true, 0, '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotTrue(...)' would fit more here.">$this->assertNotSame(true, 0, '')</weak_warning>;
        <weak_warning descr="[EA] 'assertTrue(...)' would fit more here.">$this->assertSame(0, true, '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotTrue(...)' would fit more here.">$this->assertNotSame(0, true, '')</weak_warning>;

        <weak_warning descr="[EA] 'assertFalse(...)' would fit more here.">$this->assertSame(false, 0, '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotFalse(...)' would fit more here.">$this->assertNotSame(false, 0, '')</weak_warning>;
        <weak_warning descr="[EA] 'assertFalse(...)' would fit more here.">$this->assertSame(0, false, '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotFalse(...)' would fit more here.">$this->assertNotSame(0, false, '')</weak_warning>;
    }
}