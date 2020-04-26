<?php

class TestAssertInternalType
{
    public function test()
    {
        $this->assertIsNotArray([]);
        $this->assertIsNotScalar('');
        $this->assertIsArray([]);
        $this->assertIsScalar('');
    }

    public function testWithMessages()
    {
        $this->assertIsNotArray([], '');
        $this->assertIsNotScalar('', '');
        $this->assertIsArray([], '');
        $this->assertIsScalar('', '');
    }
}