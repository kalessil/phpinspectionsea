<?php

class TestAssertFileEquals
{
    public function test()
    {
        <weak_warning descr="[EA] 'assertFileEquals(...)' would fit more here.">$this->assertSame(file_get_contents(''), file_get_contents(''))</weak_warning>;
        <weak_warning descr="[EA] 'assertFileEquals(...)' would fit more here.">$this->assertEquals(file_get_contents(''), file_get_contents(''))</weak_warning>;

        <weak_warning descr="[EA] 'assertFileEquals(...)' would fit more here.">$this->assertStringEqualsFile('', file_get_contents(''))</weak_warning>;
        <weak_warning descr="[EA] 'assertFileEquals(...)' would fit more here.">$this->assertStringEqualsFile('', file_get_contents(''))</weak_warning>;

        $this->assertSame('string', file_get_contents(''));
        $this->assertEquals('string', file_get_contents(''));
    }

    public function testWithMessages()
    {
        <weak_warning descr="[EA] 'assertFileEquals(...)' would fit more here.">$this->assertSame(file_get_contents(''), file_get_contents(''), '')</weak_warning>;
        <weak_warning descr="[EA] 'assertFileEquals(...)' would fit more here.">$this->assertEquals(file_get_contents(''), file_get_contents(''), '')</weak_warning>;

        <weak_warning descr="[EA] 'assertFileEquals(...)' would fit more here.">$this->assertStringEqualsFile('', file_get_contents(''), '')</weak_warning>;
        <weak_warning descr="[EA] 'assertFileEquals(...)' would fit more here.">$this->assertStringEqualsFile('', file_get_contents(''), '')</weak_warning>;
    }

    public function assertFileEquals()
    {
        $this->assertSame(file_get_contents(''), file_get_contents(''), '');
        $this->assertEquals(file_get_contents(''), file_get_contents(''), '');
    }
}