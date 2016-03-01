<?php

class TestCase {
    public function test() {
        $this->assertEquals(0, count([]));
        $this->assertSame(0, count([]));

        $this->assertEquals(null, 1);
        $this->assertSame(1, null);

        $this->assertTrue($x instanceof \stdClass); // <- reported
    }
}