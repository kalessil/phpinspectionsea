<?php

class TestAssertEmpty
{
    public function test()
    {
        <weak_warning descr="[EA] 'assertNotEmpty(...)' would fit more here.">$this->assertNotTrue(empty($x))</weak_warning>;
        <weak_warning descr="[EA] 'assertNotEmpty(...)' would fit more here.">$this->assertFalse(empty($x))</weak_warning>;
        <weak_warning descr="[EA] 'assertEmpty(...)' would fit more here.">$this->assertTrue(empty($x))</weak_warning>;
        <weak_warning descr="[EA] 'assertEmpty(...)' would fit more here.">$this->assertNotFalse(empty($x))</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="[EA] 'assertNotEmpty(...)' would fit more here.">$this->assertNotTrue(empty($x), '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotEmpty(...)' would fit more here.">$this->assertFalse(empty($x), '')</weak_warning>;
        <weak_warning descr="[EA] 'assertEmpty(...)' would fit more here.">$this->assertTrue(empty($x), '')</weak_warning>;
        <weak_warning descr="[EA] 'assertEmpty(...)' would fit more here.">$this->assertNotFalse(empty($x), '')</weak_warning>;
    }
}