<?php

namespace BasicCasesHolder {
    interface ParentInterface                                             {}
    interface ChildInterface extends ParentInterface                      {}
    abstract class AbstractOne implements ParentInterface                 {}
    abstract class AbstractTwo implements ParentInterface, ChildInterface {}

    class ClassOne extends AbstractOne
    {}

    class ClassTwo extends AbstractTwo
    {}
}

namespace AliasingCasesHolder {
    use \Traversable as ForeachSupport;

    abstract class AbstractClass implements \Traversable {}
    class RegularClass extends AbstractClass
    {}
}