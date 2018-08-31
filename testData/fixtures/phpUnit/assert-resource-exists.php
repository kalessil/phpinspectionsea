<?php

class TestAssertResourceExists
{
    public function test()
    {
        <weak_warning descr="'assertFileExists(...)' would fit more here.">$this->assertTrue(file_exists('...'))</weak_warning>;
        <weak_warning descr="'assertFileExists(...)' would fit more here.">$this->assertNotFalse(file_exists('...'))</weak_warning>;
        <weak_warning descr="'assertFileNotExists(...)' would fit more here.">$this->assertFalse(file_exists('...'))</weak_warning>;
        <weak_warning descr="'assertFileNotExists(...)' would fit more here.">$this->assertNotTrue(file_exists('...'))</weak_warning>;

        <weak_warning descr="'assertDirectoryExists(...)' would fit more here.">$this->assertTrue(is_dir('...'))</weak_warning>;
        <weak_warning descr="'assertDirectoryExists(...)' would fit more here.">$this->assertNotFalse(is_dir('...'))</weak_warning>;
        <weak_warning descr="'assertDirectoryNotExists(...)' would fit more here.">$this->assertFalse(is_dir('...'))</weak_warning>;
        <weak_warning descr="'assertDirectoryNotExists(...)' would fit more here.">$this->assertNotTrue(is_dir('...'))</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="'assertFileExists(...)' would fit more here.">$this->assertTrue(file_exists('...'), '')</weak_warning>;
        <weak_warning descr="'assertFileExists(...)' would fit more here.">$this->assertNotFalse(file_exists('...'), '')</weak_warning>;
        <weak_warning descr="'assertFileNotExists(...)' would fit more here.">$this->assertFalse(file_exists('...'), '')</weak_warning>;
        <weak_warning descr="'assertFileNotExists(...)' would fit more here.">$this->assertNotTrue(file_exists('...'), '')</weak_warning>;

        <weak_warning descr="'assertDirectoryExists(...)' would fit more here.">$this->assertTrue(is_dir('...'), '')</weak_warning>;
        <weak_warning descr="'assertDirectoryExists(...)' would fit more here.">$this->assertNotFalse(is_dir('...'), '')</weak_warning>;
        <weak_warning descr="'assertDirectoryNotExists(...)' would fit more here.">$this->assertFalse(is_dir('...'), '')</weak_warning>;
        <weak_warning descr="'assertDirectoryNotExists(...)' would fit more here.">$this->assertNotTrue(is_dir('...'), '')</weak_warning>;
    }
}