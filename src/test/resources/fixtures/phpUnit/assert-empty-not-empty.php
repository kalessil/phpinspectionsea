<?php

class TestAssertEmpty
{
    public function test()
    {
        <weak_warning descr="assertNotEmpty should be used instead.">$this->assertNotTrue(empty($x))</weak_warning>;
        <weak_warning descr="assertNotEmpty should be used instead.">$this->assertFalse(empty($x))</weak_warning>;
        <weak_warning descr="assertEmpty should be used instead.">$this->assertTrue(empty($x))</weak_warning>;
        <weak_warning descr="assertEmpty should be used instead.">$this->assertNotFalse(empty($x))</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="assertNotEmpty should be used instead.">$this->assertNotTrue(empty($x), '')</weak_warning>;
        <weak_warning descr="assertNotEmpty should be used instead.">$this->assertFalse(empty($x), '')</weak_warning>;
        <weak_warning descr="assertEmpty should be used instead.">$this->assertTrue(empty($x), '')</weak_warning>;
        <weak_warning descr="assertEmpty should be used instead.">$this->assertNotFalse(empty($x), '')</weak_warning>;
    }
}