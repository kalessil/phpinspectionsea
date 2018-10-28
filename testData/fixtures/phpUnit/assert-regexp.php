<?php

class TestAssertRegExp
{
    public function test() {
        <weak_warning descr="assertRegExp should be used instead">$this->assertTrue(preg_match('', '...') > 0)</weak_warning>;
        <weak_warning descr="assertNotRegExp should be used instead">$this->assertFalse(preg_match('', '...') > 0)</weak_warning>;

        <weak_warning descr="assertNotRegExp should be used instead">$this->assertSame(0, preg_match('', '...'))</weak_warning>;
        <weak_warning descr="assertRegExp should be used instead">$this->assertSame(1, preg_match('', '...'))</weak_warning>;

        <weak_warning descr="assertRegExp should be used instead">$this->assertNotSame(0, preg_match('', '...'))</weak_warning>;
        <weak_warning descr="assertNotRegExp should be used instead">$this->assertNotSame(1, preg_match('', '...'))</weak_warning>;

        <weak_warning descr="assertNotRegExp should be used instead">$this->assertEquals(0, preg_match('', '...'))</weak_warning>;
        <weak_warning descr="assertRegExp should be used instead">$this->assertEquals(1, preg_match('', '...'))</weak_warning>;

        <weak_warning descr="assertRegExp should be used instead">$this->assertNotEquals(0, preg_match('', '...'))</weak_warning>;
        <weak_warning descr="assertNotRegExp should be used instead">$this->assertNotEquals(1, preg_match('', '...'))</weak_warning>;
    }

    public function testWithMessages() {
        <weak_warning descr="assertRegExp should be used instead">$this->assertTrue(preg_match('', '...') > 0, '...')</weak_warning>;
        <weak_warning descr="assertNotRegExp should be used instead">$this->assertFalse(preg_match('', '...') > 0, '...')</weak_warning>;

        <weak_warning descr="assertNotRegExp should be used instead">$this->assertSame(0, preg_match('', '...'), '...')</weak_warning>;
        <weak_warning descr="assertRegExp should be used instead">$this->assertSame(1, preg_match('', '...'), '...')</weak_warning>;

        <weak_warning descr="assertRegExp should be used instead">$this->assertNotSame(0, preg_match('', '...'), '...')</weak_warning>;
        <weak_warning descr="assertNotRegExp should be used instead">$this->assertNotSame(1, preg_match('', '...'), '...')</weak_warning>;

        <weak_warning descr="assertNotRegExp should be used instead">$this->assertEquals(0, preg_match('', '...'), '...')</weak_warning>;
        <weak_warning descr="assertRegExp should be used instead">$this->assertEquals(1, preg_match('', '...'), '...')</weak_warning>;

        <weak_warning descr="assertRegExp should be used instead">$this->assertNotEquals(0, preg_match('', '...'), '...')</weak_warning>;
        <weak_warning descr="assertNotRegExp should be used instead">$this->assertNotEquals(1, preg_match('', '...'), '...')</weak_warning>;
    }
}