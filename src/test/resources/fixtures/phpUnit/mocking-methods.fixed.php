<?php

class CasesHolderTest
{
    public function method() {
        $mock->method('...')->will($this->returnCallback(function () {}));
        $mock->method('...')->will($this->returnValue('...'));

        $mock = $this->getMockBuilder(CasesHolderTest::class)->getMock();
        $mock->method('finalMethod')->willReturn(null);
        $mock->method('missingMethod')->willReturn(null);
    }

    final public function finalMethod() {}
}