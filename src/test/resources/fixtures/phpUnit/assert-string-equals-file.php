<?php

class TestAssertStringEqualsFile {
    public function test()
    {
        $this-><weak_warning descr="assertStringEqualsFile should be used instead.">assertSame</weak_warning> (file_get_contents(''), 'string');
        $this-><weak_warning descr="assertStringEqualsFile should be used instead.">assertEquals</weak_warning> (file_get_contents(''), 'string');

        $this-><weak_warning descr="assertStringEqualsFile should be used instead.">assertSame</weak_warning> ('string', file_get_contents(''));
        $this-><weak_warning descr="assertStringEqualsFile should be used instead.">assertEquals</weak_warning> ('string', file_get_contents(''));
    }

    public function testWithMessages()
    {
        $this-><weak_warning descr="assertStringEqualsFile should be used instead.">assertSame</weak_warning> (file_get_contents(''), '', 'message');
        $this-><weak_warning descr="assertStringEqualsFile should be used instead.">assertEquals</weak_warning> (file_get_contents(''), '', 'message');

        $this-><weak_warning descr="assertStringEqualsFile should be used instead.">assertSame</weak_warning> ('', file_get_contents(''), 'message');
        $this-><weak_warning descr="assertStringEqualsFile should be used instead.">assertEquals</weak_warning> ('', file_get_contents(''), 'message');
    }
}