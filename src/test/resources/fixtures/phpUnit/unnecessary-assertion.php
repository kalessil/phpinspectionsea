<?php

class AssertsHolder
{
    public function testAsserts()
    {
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertNull($this->returnsVoid())</weak_warning>;
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertEmpty($this->returnsVoid())</weak_warning>;
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertInstanceOf(stdClass::class, $this->returnsVoid())</weak_warning>;
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertInternalType('...', $this->returnsVoid())</weak_warning>;

        $this->assertNull($this->returnsObject());
        $this->assertEmpty($this->returnsObject());
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertInstanceOf(stdClass::class, $this->returnsObject())</weak_warning>;
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertInternalType('...', $this->returnsObject())</weak_warning>;
    }

    public function testVariants() {
        $void = $this->returnsVoid();
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertNull($void)</weak_warning>;
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertEmpty($void)</weak_warning>;
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertInstanceOf(stdClass::class, $void)</weak_warning>;
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertInternalType('...', $void)</weak_warning>;

        $object = $this->returnsObject();
        $this->assertNull($object);
        $this->assertEmpty($object);
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertInstanceOf(stdClass::class, $object)</weak_warning>;
        <weak_warning descr="This assertion can probably be skipped (argument implicitly declares return type).">$this->assertInternalType('...', $object)</weak_warning>;
    }

    abstract function returnsVoid(): void;
    abstract function returnsObject(): object;
}