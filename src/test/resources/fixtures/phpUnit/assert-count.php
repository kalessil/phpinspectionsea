<?php

class TestAssertCount
{
    public function test()
    {
        <weak_warning descr="'assertCount(...)' should be used instead.">$this->assertEquals(0, count([]))</weak_warning>;
        <weak_warning descr="'assertCount(...)' should be used instead.">$this->assertSame(0, count([]))</weak_warning>;
        <weak_warning descr="'assertNotCount(...)' should be used instead.">$this->assertNotEquals(0, count([]))</weak_warning>;
        <weak_warning descr="'assertNotCount(...)' should be used instead.">$this->assertNotSame(0, count([]))</weak_warning>;

        $this->assertEquals(count([]), 0);
        $this->assertSame(count([]), 0);
    }

    public function testWithMessages()
    {
        <weak_warning descr="'assertCount(...)' should be used instead.">$this->assertEquals(0, count([]), '')</weak_warning>;
        <weak_warning descr="'assertCount(...)' should be used instead.">$this->assertSame(0, count([]), '')</weak_warning>;
        <weak_warning descr="'assertNotCount(...)' should be used instead.">$this->assertNotEquals(0, count([]), '')</weak_warning>;
        <weak_warning descr="'assertNotCount(...)' should be used instead.">$this->assertNotSame(0, count([]), '')</weak_warning>;
    }
}