<?php

class TestAssertCount
{
    public function test()
    {
        <weak_warning descr="[EA] 'assertCount(...)' would fit more here.">$this->assertEquals(0, count([]))</weak_warning>;
        <weak_warning descr="[EA] 'assertCount(...)' would fit more here.">$this->assertSame(0, count([]))</weak_warning>;
        <weak_warning descr="[EA] 'assertNotCount(...)' would fit more here.">$this->assertNotEquals(0, count([]))</weak_warning>;
        <weak_warning descr="[EA] 'assertNotCount(...)' would fit more here.">$this->assertNotSame(0, count([]))</weak_warning>;

        $this->assertEquals(count([]), 0);
        $this->assertSame(count([]), 0);
    }

    public function testWithMessages()
    {
        <weak_warning descr="[EA] 'assertCount(...)' would fit more here.">$this->assertEquals(0, count([]), '')</weak_warning>;
        <weak_warning descr="[EA] 'assertCount(...)' would fit more here.">$this->assertSame(0, count([]), '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotCount(...)' would fit more here.">$this->assertNotEquals(0, count([]), '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotCount(...)' would fit more here.">$this->assertNotSame(0, count([]), '')</weak_warning>;
    }
}