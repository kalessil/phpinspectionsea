<?php

class CasesHolder
{
    public function testExpectsOnce() {
        $mock = $this->getMockBuilder(\SimpleXMLElement::class)->getMock();
        $mock->expects(<weak_warning descr="'->once()' would make more sense here.">$this->exactly(1)</weak_warning>)->method('asXML')->willReturn('...');
    }
}