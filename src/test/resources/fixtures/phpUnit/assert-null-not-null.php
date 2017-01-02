<?php

class TestCaseAssertNullNotNull
{
    public function test()
    {
        <weak_warning descr="assertNull should be used instead.">$this->assertSame(1, null)</weak_warning>;
        <weak_warning descr="assertNull should be used instead.">$this->assertSame(null, 1)</weak_warning>;

        <weak_warning descr="assertNotNull should be used instead.">$this->assertNotSame(1, null)</weak_warning>;
        <weak_warning descr="assertNotNull should be used instead.">$this->assertNotSame(null, 1)</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="assertNull should be used instead.">$this->assertSame(1, null, '')</weak_warning>;
        <weak_warning descr="assertNull should be used instead.">$this->assertSame(null, 1, '')</weak_warning>;

        <weak_warning descr="assertNotNull should be used instead.">$this->assertNotSame(1, null, '')</weak_warning> ;
        <weak_warning descr="assertNotNull should be used instead.">$this->assertNotSame(null, 1, '')</weak_warning>;
    }
}