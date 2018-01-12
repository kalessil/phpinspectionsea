<?php

class TestCaseAssertConstant
{
    public function testNormalization()
    {
        <weak_warning descr="'assertNotTrue(...)' should be used instead.">$this->assertTrue(!$s)</weak_warning>;
        <weak_warning descr="'assertNotFalse(...)' should be used instead.">$this->assertFalse(!$y)</weak_warning>;
    }

    public function testNormalizationWithMessage()
    {
        <weak_warning descr="'assertNotTrue(...)' should be used instead.">$this->assertTrue(!$s, '')</weak_warning>;
        <weak_warning descr="'assertNotFalse(...)' should be used instead.">$this->assertFalse(!$y, '')</weak_warning>;
    }

    public function test()
    {
        <weak_warning descr="'assertNull(...)' should be used instead.">$this->assertSame(1, null)</weak_warning>;
        <weak_warning descr="'assertNull(...)' should be used instead.">$this->assertSame(null, 1)</weak_warning>;
        <weak_warning descr="'assertNotNull(...)' should be used instead.">$this->assertNotSame(1, null)</weak_warning>;
        <weak_warning descr="'assertNotNull(...)' should be used instead.">$this->assertNotSame(null, 1)</weak_warning>;

        <weak_warning descr="'assertTrue(...)' should be used instead.">$this->assertSame(true, 0)</weak_warning>;
        <weak_warning descr="'assertNotTrue(...)' should be used instead.">$this->assertNotSame(true, 0)</weak_warning>;
        <weak_warning descr="'assertTrue(...)' should be used instead.">$this->assertSame(0, true)</weak_warning>;
        <weak_warning descr="'assertNotTrue(...)' should be used instead.">$this->assertNotSame(0, true)</weak_warning>;

        <weak_warning descr="'assertFalse(...)' should be used instead.">$this->assertSame(false, 0)</weak_warning>;
        <weak_warning descr="'assertNotFalse(...)' should be used instead.">$this->assertNotSame(false, 0)</weak_warning>;
        <weak_warning descr="'assertFalse(...)' should be used instead.">$this->assertSame(0, false)</weak_warning>;
        <weak_warning descr="'assertNotFalse(...)' should be used instead.">$this->assertNotSame(0, false)</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="'assertNull(...)' should be used instead.">$this->assertSame(1, null, '')</weak_warning>;
        <weak_warning descr="'assertNull(...)' should be used instead.">$this->assertSame(null, 1, '')</weak_warning>;
        <weak_warning descr="'assertNotNull(...)' should be used instead.">$this->assertNotSame(1, null, '')</weak_warning> ;
        <weak_warning descr="'assertNotNull(...)' should be used instead.">$this->assertNotSame(null, 1, '')</weak_warning>;

        <weak_warning descr="'assertTrue(...)' should be used instead.">$this->assertSame(true, 0, '')</weak_warning>;
        <weak_warning descr="'assertNotTrue(...)' should be used instead.">$this->assertNotSame(true, 0, '')</weak_warning>;
        <weak_warning descr="'assertTrue(...)' should be used instead.">$this->assertSame(0, true, '')</weak_warning>;
        <weak_warning descr="'assertNotTrue(...)' should be used instead.">$this->assertNotSame(0, true, '')</weak_warning>;

        <weak_warning descr="'assertFalse(...)' should be used instead.">$this->assertSame(false, 0, '')</weak_warning>;
        <weak_warning descr="'assertNotFalse(...)' should be used instead.">$this->assertNotSame(false, 0, '')</weak_warning>;
        <weak_warning descr="'assertFalse(...)' should be used instead.">$this->assertSame(0, false, '')</weak_warning>;
        <weak_warning descr="'assertNotFalse(...)' should be used instead.">$this->assertNotSame(0, false, '')</weak_warning>;
    }
}