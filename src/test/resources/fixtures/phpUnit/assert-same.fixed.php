<?php

class TestAssertSame
{
    public function test(array $x, \stdClass $y)
    {
        $this->assertSame(2, 1);
        $this->assertNotSame(2, 1);

        $this->assertEquals($x, $x);
        $this->assertEquals($y, $y);
        $this->assertEquals(1, []);
    }

    public function testWithMessages(array $x, \stdClass $y)
    {
        $this->assertSame(2, 1, '');
        $this->assertNotSame(2, 1, '');

        $this->assertEquals($x, $x, '');
        $this->assertEquals($y, $y, '');
        $this->assertEquals(1, [], '');
    }
}