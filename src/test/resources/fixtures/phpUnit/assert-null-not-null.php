<?php

class TestCaseAssertNullNotNull
{
    public function test()
    {
        $this->assertSame(1, null);
        $this->assertSame(null, 1);

        $this->assertNotSame(1, null);
        $this->assertNotSame(null, 1);
    }

    public function testWithMessages()
    {
        $this->assertSame(1, null, '');
        $this->assertSame(null, 1, '');

        $this->assertNotSame(1, null, '');
        $this->assertNotSame(null, 1, '');
    }
}