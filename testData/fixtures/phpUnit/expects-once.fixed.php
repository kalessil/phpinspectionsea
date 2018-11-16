<?php

class CasesHolder
{
    public function testExpectsOnce() {
        $mock = $this->createMock(\SimpleXMLElement::class);
        $mock->expects($this->once())->method('asXML')->willReturn('...');
    }
}