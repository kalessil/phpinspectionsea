<?php

class TestAssertCount
{
    public function test()
    {
        $this->assertCount(0, []);
        $this->assertCount(0, []);
        $this->assertNotCount(0, []);
        $this->assertNotCount(0, []);

        $this->assertEquals(count([]), 0);
        $this->assertSame(count([]), 0);
    }

    public function testWithMessages()
    {
        $this->assertCount(0, [], '');
        $this->assertCount(0, [], '');
        $this->assertNotCount(0, [], '');
        $this->assertNotCount(0, [], '');
    }
}