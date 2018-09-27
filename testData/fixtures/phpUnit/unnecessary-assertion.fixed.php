<?php

class AssertsHolder
{
    public function testAsserts()
    {
        $this->assertNull($this->returnsVoid());
        $this->assertEmpty($this->returnsVoid());
        $this->assertInstanceOf(\stdClass::class, $this->returnsStdClass());
        $this->assertInstanceOf(\stdClass::class, $this->returnsObject());
        $this->assertInternalType('...', $this->returnsVoid());

        $this->assertNull($this->returnsObject());
        $this->assertEmpty($this->returnsObject());
        $this->assertInstanceOf(\stdClass::class, $this->returnsObject());
        $this->assertInternalType('...', $this->returnsObject());
    }

    public function testVariants() {
        $void = $this->returnsVoid();
        $this->assertNull($void);
        $this->assertEmpty($void);
        $this->assertInstanceOf(\stdClass::class, $void);
        $this->assertInternalType('...', $void);

        $object = $this->returnsObject();
        $this->assertNull($object);
        $this->assertEmpty($object);
        $this->assertInstanceOf(\stdClass::class, $object);
        $this->assertInternalType('...', $object);
    }

    public function testExpectsAny() {
        $mock = $this->getMockBuilder(\SimpleXMLElement::class)->getMock();
        $mock->method('asXML')->willReturn('...');
    }

    abstract function returnsVoid(): void;
    abstract function returnsObject(): object;
    abstract function returnsStdClass(): \stdClass;
}