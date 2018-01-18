<?php

class TestAssertStringEqualsFile
{
    public function test()
    {
        $this->assertStringEqualsFile('', 'string');
        $this->assertStringEqualsFile('', 'string');

        $this->assertSame('string', file_get_contents(''));
        $this->assertEquals('string', file_get_contents(''));
    }

    public function testWithMessages()
    {
        $this->assertStringEqualsFile('', '', 'message');
        $this->assertStringEqualsFile('', '', 'message');

        $this->assertSame('', file_get_contents(''), 'message');
        $this->assertEquals('', file_get_contents(''), 'message');
    }

    public function assertStringEqualsFile()
    {
        $this->assertSame(file_get_contents(''), '', 'message');
        $this->assertEquals('', file_get_contents(''), 'message');
    }
}