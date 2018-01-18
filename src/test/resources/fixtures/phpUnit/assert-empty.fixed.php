<?php

class TestAssertEmpty
{
    public function test()
    {
        $this->assertNotEmpty($x);
        $this->assertNotEmpty($x);
        $this->assertEmpty($x);
        $this->assertEmpty($x);
    }

    public function testWithMessages()
    {
        $this->assertNotEmpty($x, '');
        $this->assertNotEmpty($x, '');
        $this->assertEmpty($x, '');
        $this->assertEmpty($x, '');
    }
}