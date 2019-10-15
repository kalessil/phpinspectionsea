<?php

class TestAssertInstanceof
{
    public function test()
    {
        <weak_warning descr="[EA] 'assertInstanceOf(...)' would fit more here.">$this->assertTrue($x instanceof \stdClass)</weak_warning>;
        <weak_warning descr="[EA] 'assertInstanceOf(...)' would fit more here.">$this->assertNotFalse($x instanceof \stdClass)</weak_warning>;
        <weak_warning descr="[EA] 'assertNotInstanceOf(...)' would fit more here.">$this->assertFalse($x instanceof \stdClass)</weak_warning>;
        <weak_warning descr="[EA] 'assertNotInstanceOf(...)' would fit more here.">$this->assertNotTrue($x instanceof \stdClass)</weak_warning>;

        <weak_warning descr="[EA] 'assertInstanceOf(...)' would fit more here.">$this->assertSame(get_class($x), 'stdClass')</weak_warning>;
        <weak_warning descr="[EA] 'assertInstanceOf(...)' would fit more here.">$this->assertEquals(get_class($x), 'stdClass')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotInstanceOf(...)' would fit more here.">$this->assertNotSame(get_class($x), 'stdClass')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotInstanceOf(...)' would fit more here.">$this->assertNotEquals(get_class($x), 'stdClass')</weak_warning>;
    }

    public function testWithMessages()
    {
        <weak_warning descr="[EA] 'assertInstanceOf(...)' would fit more here.">$this->assertTrue($x instanceof \stdClass, '')</weak_warning>;
        <weak_warning descr="[EA] 'assertInstanceOf(...)' would fit more here.">$this->assertNotFalse($x instanceof \stdClass, '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotInstanceOf(...)' would fit more here.">$this->assertFalse($x instanceof \stdClass, '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotInstanceOf(...)' would fit more here.">$this->assertNotTrue($x instanceof \stdClass, '')</weak_warning>;

        <weak_warning descr="[EA] 'assertInstanceOf(...)' would fit more here.">$this->assertSame(get_class($x), 'stdClass', '')</weak_warning>;
        <weak_warning descr="[EA] 'assertInstanceOf(...)' would fit more here.">$this->assertEquals(get_class($x), 'stdClass', '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotInstanceOf(...)' would fit more here.">$this->assertNotSame(get_class($x), 'stdClass', '')</weak_warning>;
        <weak_warning descr="[EA] 'assertNotInstanceOf(...)' would fit more here.">$this->assertNotEquals(get_class($x), 'stdClass', '')</weak_warning>;
    }
}