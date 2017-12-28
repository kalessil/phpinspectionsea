<?php

class AssertsHolder
{
    public function testAssets()
    {
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertNull($this->returnsVoid())</weak_warning>;
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertEmpty($this->returnsVoid())</weak_warning>;
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertInstanceOf(stdClass::class, $this->returnsVoid())</weak_warning>;
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertInternalType('', $this->returnsVoid())</weak_warning>;

        $this->assertNull($this->returnsObject());
        $this->assertEmpty($this->returnsObject());
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertInstanceOf(stdClass::class, $this->returnsObject())</weak_warning>;
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertInternalType('', $this->returnsObject())</weak_warning>;
    }

    abstract function returnsVoid(): void;
    abstract function returnsObject(): object;
}