<?php

class TestCase
{
    public function testNormalization()
    {
        $this->assertFalse(!$y);            // <- reported assertNotFalse

        $this->assertTrue(false == $x);     // <- reported assertEquals
        $this->assertNotFalse(false === $x);// <- reported assertSame
        $this->assertTrue(false != $x);     // <- reported assertNotEquals
        $this->assertNotFalse(false !== $x);// <- reported assertNotSame

        $this->assertFalse(false == $x);    // <- reported assertNotEquals
        $this->assertNotTrue(false === $x); // <- reported assertNotSame
        $this->assertFalse(false != $x);    // <- reported assertEquals
        $this->assertNotTrue(false !== $x); // <- reported assertSame
    }

    public function testNormalizationWithMessage()
    {
        $this->assertFalse(!$y, '');            // <- reported assertNotFalse

        $this->assertTrue(false == $x, '');     // <- reported assertEquals
        $this->assertNotFalse(false === $x, '');// <- reported assertSame
        $this->assertTrue(false != $x, '');     // <- reported assertNotEquals
        $this->assertNotFalse(false !== $x, '');// <- reported assertNotSame

        $this->assertFalse(false == $x, '');    // <- reported assertNotEquals
        $this->assertNotTrue(false === $x, ''); // <- reported assertNotSame
        $this->assertFalse(false != $x, '');    // <- reported assertEquals
        $this->assertNotTrue(false !== $x, ''); // <- reported assertSame
    }

    public function test()
    {
        $this->assertEquals(0, count([])); // <- reported assertCount
        $this->assertEquals(count([]), 0); // <- reported assertCount
        $this->assertSame(0, count([]));   // <- reported assertCount
        $this->assertSame(count([]), 0);   // <- reported assertCount

        $this->assertNotEquals(count([]), 0); // <- reported assertNotCount
        $this->assertNotSame(count([]), 0);   // <- reported assertNotCount

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

        $this->assertSame(false, 0);        // <- reported assertFalse
        $this->assertNotSame(false, 0);     // <- reported assertNotFalse
    }

    public function testWithMessages()
    {
        $this->assertSame(false, 0, '');       // <- reported assertFalse
        $this->assertNotSame(false, 0, '');    // <- reported assertNotFalse

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

        $this->assertEquals(0, count([]), ''); // <- reported assertCount
        $this->assertEquals(count([]), 0, ''); // <- reported assertCount
        $this->assertSame(0, count([]), '');   // <- reported assertCount
        $this->assertSame(count([]), 0, '');   // <- reported assertCount
    }
}