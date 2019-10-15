<?php

class CasesHolder
{
    public function testExpectsOnce() {
        $mock = $this->createMock(\SimpleXMLElement::class);
        $mock->expects(<weak_warning descr="[EA] '->once()' would make more sense here.">$this->exactly(1)</weak_warning>)->method('asXML')->willReturn('...');
    }
}