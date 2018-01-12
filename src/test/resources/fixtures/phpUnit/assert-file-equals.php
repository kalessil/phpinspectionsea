<?php

class TestAssertFileEquals
{
    public function test()
    {
        <weak_warning descr="'assertFileEquals(...)' should be used instead.">$this->assertSame(file_get_contents(''), file_get_contents(''))</weak_warning>;
        <weak_warning descr="'assertFileEquals(...)' should be used instead.">$this->assertEquals(file_get_contents(''), file_get_contents(''))</weak_warning>;

        $this->assertSame('string', file_get_contents(''));
        $this->assertEquals('string', file_get_contents(''));
    }

    public function testWithMessages()
    {
        <weak_warning descr="'assertFileEquals(...)' should be used instead.">$this->assertSame(file_get_contents(''), file_get_contents(''), '')</weak_warning>;
        <weak_warning descr="'assertFileEquals(...)' should be used instead.">$this->assertEquals(file_get_contents(''), file_get_contents(''), '')</weak_warning>;
    }
}