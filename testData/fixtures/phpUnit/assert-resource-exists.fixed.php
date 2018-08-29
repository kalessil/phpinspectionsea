<?php

class TestAssertResourceExists
{
    public function test()
    {
        $this->assertFileExists('...');
        $this->assertFileExists('...');
        $this->assertFileNotExists('...');
        $this->assertFileNotExists('...');

        $this->assertDirectoryExists('...');
        $this->assertDirectoryExists('...');
        $this->assertDirectoryNotExists('...');
        $this->assertDirectoryNotExists('...');
    }

    public function testWithMessages()
    {
        $this->assertFileExists('...', '');
        $this->assertFileExists('...', '');
        $this->assertFileNotExists('...', '');
        $this->assertFileNotExists('...', '');

        $this->assertDirectoryExists('...', '');
        $this->assertDirectoryExists('...', '');
        $this->assertDirectoryNotExists('...', '');
        $this->assertDirectoryNotExists('...', '');
    }
}