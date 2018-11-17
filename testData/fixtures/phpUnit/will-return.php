<?php

class CasesHolder
{
    public function testWillReturn() {
        $mock = $this->createMock(\SimpleXMLElement::class);
        <weak_warning descr="'->willReturn(...)' would make more sense here.">$mock->method('asXML')->will($this->returnValue('...'))</weak_warning>;
    }
}