<?php

class TestAssertFileExists
{
    public function test()
    {
        <weak_warning descr="assertDirectoryExists should be used instead.">$this->assertTrue(is_dir('...'))</weak_warning>;
        <weak_warning descr="assertDirectoryExists should be used instead.">$this->assertNotFalse(is_dir('...'))</weak_warning>;
        <weak_warning descr="assertDirectoryNotExists should be used instead.">$this->assertFalse(is_dir('...'))</weak_warning>;
        <weak_warning descr="assertDirectoryNotExists should be used instead.">$this->assertNotTrue(is_dir('...'))</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="assertDirectoryExists should be used instead.">$this->assertTrue(is_dir('...'), '')</weak_warning>;
        <weak_warning descr="assertDirectoryExists should be used instead.">$this->assertNotFalse(is_dir('...'), '')</weak_warning>;
        <weak_warning descr="assertDirectoryNotExists should be used instead.">$this->assertFalse(is_dir('...'), '')</weak_warning>;
        <weak_warning descr="assertDirectoryNotExists should be used instead.">$this->assertNotTrue(is_dir('...'), '')</weak_warning>;
    }
}