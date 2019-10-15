<?php

class SampleTest {
    public function pass()
    {
        $mock = $this->getMockBuilder(Service::class)->setConstructorArgs([$this->createMock(Clazz::class)])->getMock();
        $service = new Service($this->createMock(Clazz::class));
    }

    public function <warning descr="[EA] 2 mocks has been introduced here: either the test case should be refactored or API has design issues.">complain</warning>()
    {
        $mock = $this->getMockBuilder(Service::class)->setConstructorArgs([$this->createMock(Clazz::class)])->getMock();
        $service = new Service($this->createMock(Clazz::class));

        $trigger = $this->createMock(Clazz::class);
    }

    public function <error descr="[EA] 3 mocks has been introduced here: either the test case should be refactored or API has design issues.">scream</error>()
    {
        $mock = $this->getMockBuilder(Service::class)->setConstructorArgs([$this->createMock(Clazz::class)])->getMock();
        $service = new Service($this->createMock(Clazz::class));

        $trigger = $this->createMock(Clazz::class);
        $trigger = $this->createMock(Clazz::class);
    }
}