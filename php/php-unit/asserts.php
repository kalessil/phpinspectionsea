<?php

class TestCase
{
    public function test()
    {
        $this->assertEquals(2, 1);            // <- reported, assertSame    IF strict options set
        $this->assertNotEquals(2, 1);         // <- reported, assertnotSame IF strict options set

        $this->assertTrue($x instanceof \stdClass);     // <- reported assertInstanceOf
        $this->assertNotFalse($x instanceof \stdClass); // <- reported assertInstanceOf
        $this->assertFalse($x instanceof \stdClass);    // <- reported assertNotInstanceOf
        $this->assertNotTrue($x instanceof \stdClass);  // <- reported assertNotInstanceOf

        $this->assertTrue(file_exists($x));  // <- reported assertFileExists
        $this->assertFalse(file_exists($x)); // <- reported assertFileNotExists

        $this->assertNotTrue(empty($x));   // <- reported assertNotEmpty
        $this->assertFalse(empty($x));     // <- reported assertNotEmpty
        $this->assertTrue(empty($x));      // <- reported assertEmpty
        $this->assertNotFalse(empty($x));  // <- reported assertEmpty
    }

    public function testWithMessages()
    {
        $this->assertNotTrue(empty($x), '');   // <- reported assertNotEmpty
        $this->assertFalse(empty($x), '');     // <- reported assertNotEmpty
        $this->assertTrue(empty($x), '');      // <- reported assertEmpty
        $this->assertNotFalse(empty($x), '');  // <- reported assertEmpty

        $this->assertEquals(2, 1, '');         // <- reported, assertSame    IF strict options set
        $this->assertNotEquals(2, 1, '');      // <- reported, assertnotSame IF strict options set

        $this->assertTrue($x instanceof \stdClass, '');     // <- reported assertInstanceOf
        $this->assertNotFalse($x instanceof \stdClass, ''); // <- reported assertInstanceOf
        $this->assertFalse($x instanceof \stdClass, '');    // <- reported assertNotInstanceOf
        $this->assertNotTrue($x instanceof \stdClass, '');  // <- reported assertNotInstanceOf

        $this->assertTrue(file_exists($x), '');  // <- reported assertFileExists
        $this->assertFalse(file_exists($x), ''); // <- reported assertFileNotExists
    }
}