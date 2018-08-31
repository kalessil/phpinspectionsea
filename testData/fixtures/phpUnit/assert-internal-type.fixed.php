<?php

class TestAssertInternalType
{
    public function test()
    {
        $this->assertNotInternalType('array', []);
        $this->assertNotInternalType('scalar', '');
        $this->assertInternalType('array', []);
        $this->assertInternalType('scalar', '');
    }

    public function testWithMessages()
    {
        $this->assertNotInternalType('array', [], '');
        $this->assertNotInternalType('scalar', '', '');
        $this->assertInternalType('array', [], '');
        $this->assertInternalType('scalar', '', '');
    }
}