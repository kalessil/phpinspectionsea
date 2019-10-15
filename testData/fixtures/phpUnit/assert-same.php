<?php

class TestAssertSame
{
    public function test(array $x, \stdClass $y)
    {
        <weak_warning descr="[EA] This check is type-unsafe, consider using 'assertSame(...)' instead.">$this->assertEquals(2, 1)</weak_warning>;
        <weak_warning descr="[EA] This check is type-unsafe, consider using 'assertNotSame(...)' instead.">$this->assertNotEquals(2, 1)</weak_warning>;

        $this->assertEquals($x, $x);
        $this->assertEquals($y, $y);
        $this->assertEquals(1, []);
    }

    public function testWithMessages(array $x, \stdClass $y)
    {
        <weak_warning descr="[EA] This check is type-unsafe, consider using 'assertSame(...)' instead.">$this->assertEquals(2, 1, '')</weak_warning>;
        <weak_warning descr="[EA] This check is type-unsafe, consider using 'assertNotSame(...)' instead.">$this->assertNotEquals(2, 1, '')</weak_warning>;

        $this->assertEquals($x, $x, '');
        $this->assertEquals($y, $y, '');
        $this->assertEquals(1, [], '');
    }
}