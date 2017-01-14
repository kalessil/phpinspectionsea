<?php

class TestAssertStringEqualsFile {
    public function test()
    {
        <weak_warning descr="assertStringEqualsFile should be used instead.">$this->assertSame (file_get_contents(''), 'string')</weak_warning>;
        <weak_warning descr="assertStringEqualsFile should be used instead.">$this->assertEquals (file_get_contents(''), 'string')</weak_warning>;

        <weak_warning descr="assertStringEqualsFile should be used instead.">$this->assertSame ('string', file_get_contents(''))</weak_warning>;
        <weak_warning descr="assertStringEqualsFile should be used instead.">$this->assertEquals ('string', file_get_contents(''))</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="assertStringEqualsFile should be used instead.">$this->assertSame (file_get_contents(''), '', 'message')</weak_warning>;
        <weak_warning descr="assertStringEqualsFile should be used instead.">$this->assertEquals (file_get_contents(''), '', 'message')</weak_warning>;

        <weak_warning descr="assertStringEqualsFile should be used instead.">$this->assertSame ('', file_get_contents(''), 'message')</weak_warning>;
        <weak_warning descr="assertStringEqualsFile should be used instead.">$this->assertEquals ('', file_get_contents(''), 'message')</weak_warning>;
    }
}