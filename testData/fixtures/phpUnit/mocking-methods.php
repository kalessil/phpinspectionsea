<?php

class CasesHolderTest
{
    public function method() {
        $mock->method('method')-><warning descr="[EA] It probably was intended to use '->will(...)' here.">willReturn</warning>($this->returnCallback(function () {}));
        $mock->method('method')-><warning descr="[EA] It probably was intended to use '->will(...)' here.">willReturn</warning>($this->returnValue('...'));

        $mock = $this->getMockBuilder(CasesHolderTest::class)
            ->setMethods(['injectedMethod'])
            ->getMock();

        $mock->method(<error descr="[EA] The method is final hence can not be mocked.">'finalMethod'</error>)->willReturn(null);
        $mock->method(<error descr="[EA] The method was not resolved, perhaps it doesn't exist.">'missingMethod'</error>)->willReturn(null);

        $mock->expects()->method(<error descr="[EA] The method is final hence can not be mocked.">'finalMethod'</error>)->willReturn(null);
        $mock->expects()->method(<error descr="[EA] The method was not resolved, perhaps it doesn't exist.">'missingMethod'</error>)->willReturn(null);

        $mock->method('method')->willReturn(null);
        $mock->method('injectedMethod')->willReturn(null);
    }

    final public function finalMethod() {}
}