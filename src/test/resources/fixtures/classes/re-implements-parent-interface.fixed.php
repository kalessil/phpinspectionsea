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

    class ClassImplementsSameInterfaceTwice
        implements
            ChildInterface
    {}
}

namespace AliasingCasesHolder {
    use \Traversable as ForeachSupport;

    class ClassImplementsSameInterfaceTwice
        implements
            \Traversable
    {}

    abstract class AbstractClass implements \Traversable {}
    class RegularClass extends AbstractClass
    {}
}