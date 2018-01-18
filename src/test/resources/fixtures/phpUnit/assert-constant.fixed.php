<?php

class TestCaseAssertConstant
{
    public function testNormalization()
    {
        $this->assertNotTrue($s);
        $this->assertNotFalse($y);
    }

    public function testNormalizationWithMessage()
    {
        $this->assertNotTrue($s, '');
        $this->assertNotFalse($y, '');
    }

    public function test()
    {
        $this->assertNull(1);
        $this->assertNull(1);
        $this->assertNotNull(1);
        $this->assertNotNull(1);

        $this->assertTrue(0);
        $this->assertNotTrue(0);
        $this->assertTrue(0);
        $this->assertNotTrue(0);

        $this->assertFalse(0);
        $this->assertNotFalse(0);
        $this->assertFalse(0);
        $this->assertNotFalse(0);
    }

    public function testWithMessages()
    {
        $this->assertNull(1, '');
        $this->assertNull(1, '');
        $this->assertNotNull(1, '') ;
        $this->assertNotNull(1, '');

        $this->assertTrue(0, '');
        $this->assertNotTrue(0, '');
        $this->assertTrue(0, '');
        $this->assertNotTrue(0, '');

        $this->assertFalse(0, '');
        $this->assertNotFalse(0, '');
        $this->assertFalse(0, '');
        $this->assertNotFalse(0, '');
    }
}