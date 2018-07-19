<?php

namespace PhpSpec {
    class ObjectBehavior {}
}

namespace PHPUnit\Framework {
    class TestCase
    {
        public function getMockBuilder()          {}
        public function getMockForTrait()         {}
        public function getMockForAbstractClass() {}
        public function getMockClass()            {}
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
    final class FinalClazz {}
    abstract class AbstractClazz {}
    trait TraitClazz {}

    $phpUnitOld = new \PHPUnit_Framework_TestCase();
    $phpUnitNew = new \PHPUnit\Framework\TestCase();

    $phpUnitOld->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">FinalClazz::class</error>);
    $phpUnitOld->getMock(<error descr="Causes reflection errors as the referenced class is final.">FinalClazz::class</error>);
    $phpUnitOld->getMockClass(<error descr="Causes reflection errors as the referenced class is final.">FinalClazz::class</error>);
    $phpUnitOld->getMockBuilder(stdClass::class);

    $phpUnitNew->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">FinalClazz::class</error>);
    $phpUnitNew->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">'FinalClazz'</error>);
    $phpUnitNew->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">'\FinalClazz'</error>);
    $phpUnitNew->getMockBuilder(<error descr="Causes reflection errors as the referenced class is final.">"\\FinalClazz"</error>);
    $phpUnitNew->getMockClass(<error descr="Causes reflection errors as the referenced class is final.">FinalClazz::class</error>);
    $phpUnitNew->getMockBuilder(stdClass::class);

    $phpUnitNew->getMockForAbstractClass(<error descr="Needs an abstract class here.">FinalClazz::class</error>);
    $phpUnitNew->getMockForTrait(<error descr="Needs a trait here.">FinalClazz::class</error>);

    $phpUnitOld->getMockBuilder(<error descr="Perhaps it was intended to mock it with getMockForAbstractClass method.">AbstractClazz::class</error>);
    $phpUnitOld->getMockBuilder(<error descr="Perhaps it was intended to mock it with getMockForTrait method.">TraitClazz::class</error>);

    class ClazzSpec extends \PhpSpec\ObjectBehavior
    {
        function it_does_something(<error descr="Causes reflection errors as the referenced class is final.">FinalClazz</error> $clazz) {}
    }
}