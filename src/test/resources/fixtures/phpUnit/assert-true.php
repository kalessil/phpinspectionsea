<?php

class TestCaseAssertTrueNotTrue
{
    public function testNormalization()
    {
        <weak_warning descr="'assertNotTrue(...)' should be used instead.">$this->assertTrue(!$s)</weak_warning>;
    }

    public function testNormalizationWithMessage()
    {
        <weak_warning descr="'assertNotTrue(...)' should be used instead.">$this->assertTrue(!$s, '')</weak_warning>;
    }

    public function test()
    {
        <weak_warning descr="'assertTrue(...)' should be used instead.">$this->assertSame(true, 0)</weak_warning>;
        <weak_warning descr="'assertNotTrue(...)' should be used instead.">$this->assertNotSame(true, 0)</weak_warning>;

        <weak_warning descr="'assertTrue(...)' should be used instead.">$this->assertSame(0, true)</weak_warning>;
        <weak_warning descr="'assertNotTrue(...)' should be used instead.">$this->assertNotSame(0, true)</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="'assertTrue(...)' should be used instead.">$this->assertSame(true, 0, '')</weak_warning>;
        <weak_warning descr="'assertNotTrue(...)' should be used instead.">$this->assertNotSame(true, 0, '')</weak_warning>;

        <weak_warning descr="'assertTrue(...)' should be used instead.">$this->assertSame(0, true, '')</weak_warning>;
        <weak_warning descr="'assertNotTrue(...)' should be used instead.">$this->assertNotSame(0, true, '')</weak_warning>;
    }
}