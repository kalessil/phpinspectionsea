<?php

class TestCase
{
    public function test()
    {
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

        $this->assertTrue(file_exists($x), '');  // <- reported assertFileExists
        $this->assertFalse(file_exists($x), ''); // <- reported assertFileNotExists
    }
}