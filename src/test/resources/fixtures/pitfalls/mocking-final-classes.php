<?php

namespace PhpSpec {
    class ObjectBehavior {}
}

namespace PHPUnit\Framework {
    class TestCase
    {
        public function getMockBuilder() {}
        public function getMockClass()   {}
    }
}
namespace {
    class PHPUnit_Framework_TestCase
    {
        public function getMockBuilder() {}
        public function getMock()        {}
        public function getMockClass()   {}
    }
}

namespace {
    final class Clazz extends stdClass {}

    $phpUnitOld = new \PHPUnit_Framework_TestCase();
    $phpUnitNew = new \PHPUnit\Framework\TestCase();

    $phpUnitOld->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">Clazz::class</error>);
    $phpUnitOld->getMock(<error descr="Causes reflection errors as the referenced class is final.">Clazz::class</error>);
    $phpUnitOld->getMockClass(<error descr="Causes reflection errors as the referenced class is final.">Clazz::class</error>);
    $phpUnitOld->getMockBuilder(stdClass::class);

    $phpUnitNew->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">Clazz::class</error>);
    $phpUnitNew->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">'Clazz'</error>);
    $phpUnitNew->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">'\Clazz'</error>);
    $phpUnitNew->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">"\\Clazz"</error>);
    $phpUnitNew->getMockClass(<error descr="Causes reflection errors as the referenced class is final.">Clazz::class</error>);
    $phpUnitNew->getMockBuilder(stdClass::class);

    class ClazzSpec extends \PhpSpec\ObjectBehavior
    {
        function it_does_something(<error descr="Causes reflection errors as the referenced class is final.">Clazz</error> $clazz) {}
    }
}