<?php

class CasesHolderTest
{
    public function method() {
        $mock->method('...')->will($this->returnCallback(function () {}));
        $mock->method('...')->will($this->returnValue('...'));
    }
}