<?php

class CasesHolder
{
    public function testWillReturn() {
        $mock = $this->createMock(\SimpleXMLElement::class);
        $mock->method('asXML')->willReturn('...');
    }
}