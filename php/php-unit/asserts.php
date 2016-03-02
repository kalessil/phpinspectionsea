<?php

class TestCase
{
    public function test()
    {
        $this->assertEquals(0, count([])); // <- reported
        $this->assertEquals(count([]), 0); // <- reported

        $this->assertSame(0, count([]));   // <- reported
        $this->assertSame(count([]), 0);   // <- reported

        $this->assertEquals(null, 1);      // <- reported, IF strict options set
        $this->assertSame(1, null);        // <- reported

        $this->assertTrue($x instanceof \stdClass); // <- reported
    }
}