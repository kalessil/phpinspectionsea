<?php

namespace PhpSpec {
    class ObjectBehavior
    {
        public function shouldHaveType()    {}
        public function shouldNotHaveType() {}
    }
}
namespace PHPUnit\Framework {
    class TestCase                   { public function getMockBuilder() {} }
}
namespace {
    class PHPUnit_Framework_TestCase { public function getMockBuilder() {} }
}

namespace {
    final class Clazz extends stdClass {}

    $phpSpec    = new \PhpSpec\ObjectBehavior();
    $phpUnitOld = new \PHPUnit_Framework_TestCase();
    $phpUnitNew = new \PHPUnit\Framework\TestCase();

    $phpSpec->shouldHaveType(<error descr="Causes reflection errors as the referenced class is final.">Clazz::class</error>);
    $phpSpec->shouldNotHaveType(<error descr="Causes reflection errors as the referenced class is final.">Clazz::class</error>);
    $phpSpec->shouldHaveType(stdClass::class);
    $phpSpec->shouldNotHaveType(stdClass::class);

    $phpUnitOld->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">Clazz::class</error>);
    $phpUnitOld->getMockBuilder(stdClass::class);

    $phpUnitNew->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">Clazz::class</error>);
    $phpUnitNew->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">'Clazz'</error>);
    $phpUnitNew->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">'\Clazz'</error>);
    $phpUnitNew->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">"\\Clazz"</error>);
    $phpUnitNew->getMockBuilder(stdClass::class);
}