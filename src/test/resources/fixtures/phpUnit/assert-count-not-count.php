<?php

class TestAssertCount
{
    public function test()
    {
        <weak_warning descr="assertCount should be used instead.">$this->assertEquals (0, count([]))</weak_warning>;
        <weak_warning descr="assertCount should be used instead.">$this->assertEquals (count([]), 0)</weak_warning>;
        <weak_warning descr="assertCount should be used instead.">$this->assertSame (0, count([]))</weak_warning>;
        <weak_warning descr="assertCount should be used instead.">$this->assertSame (count([]), 0)</weak_warning>;

        <weak_warning descr="assertNotCount should be used instead.">$this->assertNotEquals (count([]), 0)</weak_warning>;
        <weak_warning descr="assertNotCount should be used instead.">$this->assertNotSame (count([]), 0)</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="assertCount should be used instead.">$this->assertEquals (0, count([]), '')</weak_warning>;
        <weak_warning descr="assertCount should be used instead.">$this->assertEquals (count([]), 0, '')</weak_warning>;
        <weak_warning descr="assertCount should be used instead.">$this->assertSame (0, count([]), '')</weak_warning>;
        <weak_warning descr="assertCount should be used instead.">$this->assertSame (count([]), 0, '')</weak_warning>;
    }
}