<?php

class CasesHolderTest
{
    public function method() {
        $mock->method('method')->will($this->returnCallback(function () {}));
        $mock->method('method')->will($this->returnValue('...'));

        $mock = $this->getMockBuilder(CasesHolderTest::class)->getMock();

        $mock->method('finalMethod')->willReturn(null);
        $mock->method('missingMethod')->willReturn(null);

        $mock->expects()->method('finalMethod')->willReturn(null);
        $mock->expects()->method('missingMethod')->willReturn(null);

        $mock->method('method')->willReturn(null);
    }

    final public function finalMethod() {}
}