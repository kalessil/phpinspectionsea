<?php

class TestAssertResourceExists
{
    public function test()
    {
        $this->assertFileExists('...');
        $this->assertFileExists('...');
        $this->assertFileDoesNotExist('...');
        $this->assertFileDoesNotExist('...');

        $this->assertDirectoryExists('...');
        $this->assertDirectoryExists('...');
        $this->assertDirectoryDoesNotExist('...');
        $this->assertDirectoryDoesNotExist('...');
    }

    public function testWithMessages()
    {
        $this->assertFileExists('...', '');
        $this->assertFileExists('...', '');
        $this->assertFileDoesNotExist('...', '');
        $this->assertFileDoesNotExist('...', '');

        $this->assertDirectoryExists('...', '');
        $this->assertDirectoryExists('...', '');
        $this->assertDirectoryDoesNotExist('...', '');
        $this->assertDirectoryDoesNotExist('...', '');
    }
}