<?php

class TestAssertStringEqualsFile
{
    public function test()
    {
        <weak_warning descr="'assertStringEqualsFile(...)' would fit more here.">$this->assertSame(file_get_contents(''), 'string')</weak_warning>;
        <weak_warning descr="'assertStringEqualsFile(...)' would fit more here.">$this->assertEquals(file_get_contents(''), 'string')</weak_warning>;

        $this->assertSame('string', file_get_contents(''));
        $this->assertEquals('string', file_get_contents(''));
    }

    public function testWithMessages()
    {
        <weak_warning descr="'assertStringEqualsFile(...)' would fit more here.">$this->assertSame(file_get_contents(''), '', 'message')</weak_warning>;
        <weak_warning descr="'assertStringEqualsFile(...)' would fit more here.">$this->assertEquals(file_get_contents(''), '', 'message')</weak_warning>;

        $this->assertSame('', file_get_contents(''), 'message');
        $this->assertEquals('', file_get_contents(''), 'message');
    }

    public function assertStringEqualsFile()
    {
        $this->assertSame(file_get_contents(''), '', 'message');
        $this->assertEquals('', file_get_contents(''), 'message');
    }
}