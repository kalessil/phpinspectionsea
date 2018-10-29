<?php

class TestAssertRegExp
{
    public function test() {
        $this->assertRegExp('', '...');
        $this->assertNotRegExp('', '...');

        $this->assertNotRegExp('', '...');
        $this->assertRegExp('', '...');

        $this->assertRegExp('', '...');
        $this->assertNotRegExp('', '...');

        $this->assertNotRegExp('', '...');
        $this->assertRegExp('', '...');

        $this->assertRegExp('', '...');
        $this->assertNotRegExp('', '...');
    }

    public function testWithMessages() {
        $this->assertRegExp('', '...', '...');
        $this->assertNotRegExp('', '...', '...');

        $this->assertNotRegExp('', '...', '...');
        $this->assertRegExp('', '...', '...');

        $this->assertRegExp('', '...', '...');
        $this->assertNotRegExp('', '...', '...');

        $this->assertNotRegExp('', '...', '...');
        $this->assertRegExp('', '...', '...');

        $this->assertRegExp('', '...', '...');
        $this->assertNotRegExp('', '...', '...');
    }
}