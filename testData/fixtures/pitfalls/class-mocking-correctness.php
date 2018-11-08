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
        public function createMock()              {}
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
    interface InterfaceClazz {}

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

    $phpUnitNew->createMock(<error descr="Causes reflection errors as the referenced class is final.">FinalClazz::class</error>);
    $phpUnitNew->createMock(<error descr="Causes reflection errors as the referenced class is a trait.">TraitClazz::class</error>);
    $phpUnitNew->createMock(stdClass::class);

    $phpUnitNew->getMockForAbstractClass(<error descr="Needs an abstract class here.">FinalClazz::class</error>);
    $phpUnitNew->getMockForTrait(<error descr="Needs a trait here.">FinalClazz::class</error>);

    $phpUnitOld->getMockBuilder(<error descr="Perhaps it was intended to mock it with getMockForAbstractClass method.">AbstractClazz::class</error>);
    $phpUnitOld->getMockBuilder(<error descr="Perhaps it was intended to mock it with getMockForTrait method.">TraitClazz::class</error>);
    $phpUnitOld->getMockBuilder(InterfaceClazz::class);

    class ClazzSpec extends \PhpSpec\ObjectBehavior
    {
        function it_does_something(<error descr="Causes reflection errors as the referenced class is final.">FinalClazz</error> $clazz) {}
    }

    class NeedsConstructorMocking   { public function __construct($parameter){} }
    class NeedsNoConstructorMocking { public function __construct($parameter = null){} }

    $phpUnitNew->getMockBuilder(<error descr="Needs constructor to be disabled or supplied with arguments.">NeedsConstructorMocking::class</error>)->getMock();
    $phpUnitNew->getMockBuilder(NeedsNoConstructorMocking::class)->getMock();
}