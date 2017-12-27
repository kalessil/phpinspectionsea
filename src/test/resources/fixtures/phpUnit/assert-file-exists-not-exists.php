<?php

class TestAssertFileExists
{
    public function test()
    {
        <weak_warning descr="assertFileExists should be used instead.">$this->assertTrue(file_exists('...'))</weak_warning>;
        <weak_warning descr="assertFileExists should be used instead.">$this->assertNotFalse(file_exists('...'))</weak_warning>;
        <weak_warning descr="assertFileNotExists should be used instead.">$this->assertFalse(file_exists('...'))</weak_warning>;
        <weak_warning descr="assertFileNotExists should be used instead.">$this->assertNotTrue(file_exists('...'))</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="assertFileExists should be used instead.">$this->assertTrue(file_exists('...'), '')</weak_warning>;
        <weak_warning descr="assertFileExists should be used instead.">$this->assertNotFalse(file_exists('...'), '')</weak_warning>;
        <weak_warning descr="assertFileNotExists should be used instead.">$this->assertFalse(file_exists('...'), '')</weak_warning>;
        <weak_warning descr="assertFileNotExists should be used instead.">$this->assertNotTrue(file_exists('...'), '')</weak_warning>;
    }
}