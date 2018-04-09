<?php

class TestAssertCount
{
    public function test()
    {
        <weak_warning descr="'assertContains(...)' would fit more here.">$this->assertTrue(in_array('...', []))</weak_warning>;
        <weak_warning descr="'assertContains(...)' would fit more here.">$this->assertTrue(in_array('...', [], true))</weak_warning>;

        <weak_warning descr="'assertContains(...)' would fit more here.">$this->assertTrue(in_array('...', []))</weak_warning>;
        <weak_warning descr="'assertContains(...)' would fit more here.">$this->assertNotFalse(in_array('...', []))</weak_warning>;
        <weak_warning descr="'X(...)' would fit more here.">$this->assertFalse(in_array('...', []))</weak_warning>;
        <weak_warning descr="'X(...)' would fit more here.">$this->assertNotTrue(in_array('...', []))</weak_warning>;

        $this->assertTrue(in_array('...'));
        $this->assertFalse(in_array('...'));
    }

    public function testWithMessages()
    {
        <weak_warning descr="'assertContains(...)' would fit more here.">$this->assertTrue(in_array('...', []), '')</weak_warning>;
        <weak_warning descr="'assertContains(...)' would fit more here.">$this->assertTrue(in_array('...', [], true), '')</weak_warning>;

        <weak_warning descr="'assertContains(...)' would fit more here.">$this->assertTrue(in_array('...', []), '')</weak_warning>;
        <weak_warning descr="'assertContains(...)' would fit more here.">$this->assertNotFalse(in_array('...', []), '')</weak_warning>;
        <weak_warning descr="'assertNotContains(...)' would fit more here.">$this->assertFalse(in_array('...', []), '')</weak_warning>;
        <weak_warning descr="'assertNotContains(...)' would fit more here.">$this->assertNotTrue(in_array('...', []), '')</weak_warning>;
    }
}