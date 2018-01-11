<?php

class TestCaseAssetFalseNotFalse
{
    public function testNormalization()
    {
        <weak_warning descr="'assertNotFalse(...)' should be used instead.">$this->assertFalse(!$y)</weak_warning>;
    }

    public function testNormalizationWithMessage()
    {
        <weak_warning descr="'assertNotFalse(...)' should be used instead.">$this->assertFalse(!$y, '')</weak_warning>;
    }

    public function test()
    {
        <weak_warning descr="'assertFalse(...)' should be used instead.">$this->assertSame(false, 0)</weak_warning>;
        <weak_warning descr="'assertNotFalse(...)' should be used instead.">$this->assertNotSame(false, 0)</weak_warning>;

        <weak_warning descr="'assertFalse(...)' should be used instead.">$this->assertSame(0, false)</weak_warning>;
        <weak_warning descr="'assertNotFalse(...)' should be used instead.">$this->assertNotSame(0, false)</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="'assertFalse(...)' should be used instead.">$this->assertSame(false, 0, '')</weak_warning>;
        <weak_warning descr="'assertNotFalse(...)' should be used instead.">$this->assertNotSame(false, 0, '')</weak_warning>;

        <weak_warning descr="'assertFalse(...)' should be used instead.">$this->assertSame(0, false, '')</weak_warning>;
        <weak_warning descr="'assertNotFalse(...)' should be used instead.">$this->assertNotSame(0, false, '')</weak_warning>;
    }
}