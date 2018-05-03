<?php

class CasesHolderTest
{
    public function method() {
        $mock->method('...')-><warning descr="It probably was intended to use '->will(...)' here.">willReturn</warning>($this->returnCallback(function () {}));
        $mock->method('...')-><warning descr="It probably was intended to use '->will(...)' here.">willReturn</warning>($this->returnValue('...'));
    }
}