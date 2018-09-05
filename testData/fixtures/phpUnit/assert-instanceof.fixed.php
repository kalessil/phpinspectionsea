<?php

class TestAssertInstanceof
{
    public function test()
    {
        $this->assertInstanceOf(\stdClass::class, $x);
        $this->assertInstanceOf(\stdClass::class, $x);
        $this->assertNotInstanceOf(\stdClass::class, $x);
        $this->assertNotInstanceOf(\stdClass::class, $x);

        $this->assertInstanceOf(\stdClass::class, $x);
        $this->assertInstanceOf(\stdClass::class, $x);
        $this->assertNotInstanceOf(\stdClass::class, $x);
        $this->assertNotInstanceOf(\stdClass::class, $x);
    }

    public function testWithMessages()
    {
        $this->assertInstanceOf(\stdClass::class, $x, '');
        $this->assertInstanceOf(\stdClass::class, $x, '');
        $this->assertNotInstanceOf(\stdClass::class, $x, '');
        $this->assertNotInstanceOf(\stdClass::class, $x, '');

        $this->assertInstanceOf(\stdClass::class, $x, '');
        $this->assertInstanceOf(\stdClass::class, $x, '');
        $this->assertNotInstanceOf(\stdClass::class, $x, '');
        $this->assertNotInstanceOf(\stdClass::class, $x, '');
    }
}