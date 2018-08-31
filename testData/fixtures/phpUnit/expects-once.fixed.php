<?php

class CasesHolder
{
    public function testExpectsOnce() {
        $mock = $this->getMockBuilder(\SimpleXMLElement::class)->getMock();
        $mock->expects($this->once())->method('asXML')->willReturn('...');
    }
}