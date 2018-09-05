<?php

class TestAssertFileEquals
{
    public function test()
    {
        $this->assertFileEquals('', '');
        $this->assertFileEquals('', '');

        $this->assertFileEquals('', '');
        $this->assertFileEquals('', '');

        $this->assertSame('string', file_get_contents(''));
        $this->assertEquals('string', file_get_contents(''));
    }

    public function testWithMessages()
    {
        $this->assertFileEquals('', '', '');
        $this->assertFileEquals('', '', '');

        $this->assertFileEquals('', '', '');
        $this->assertFileEquals('', '', '');
    }

    public function assertFileEquals()
    {
        $this->assertSame(file_get_contents(''), file_get_contents(''), '');
        $this->assertEquals(file_get_contents(''), file_get_contents(''), '');
    }
}