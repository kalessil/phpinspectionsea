<?php

namespace BasicCasesHolder {
    interface ParentInterface                                             {}
    interface ChildInterface extends ParentInterface                      {}
    abstract class AbstractOne implements ParentInterface                 {}
    abstract class AbstractTwo implements ParentInterface, ChildInterface {}

    class ClassOne extends AbstractOne
        implements
            <error descr="'\BasicCasesHolder\ParentInterface' is already announced in '\BasicCasesHolder\AbstractOne'.">ParentInterface</error> {}

    class ClassTwo extends AbstractTwo
        implements
            <error descr="'\BasicCasesHolder\ParentInterface' is already announced in '\BasicCasesHolder\AbstractTwo'.">ParentInterface</error>,
            <error descr="'\BasicCasesHolder\ChildInterface' is already announced in '\BasicCasesHolder\AbstractTwo'.">ChildInterface</error> {}

    class ClassImplementsSameInterfaceTwice
        implements
            ChildInterface,
            <error descr="Class cannot implement previously implemented interface">ChildInterface</error> {}
}

namespace AliasingCasesHolder {
    use \Traversable as ForeachSupport;

    class ClassImplementsSameInterfaceTwice
        implements
            \Traversable,
            <error descr="Class cannot implement previously implemented interface">ForeachSupport</error> {}

    abstract class AbstractClass implements \Traversable {}
    class RegularClass extends AbstractClass
        implements
            <error descr="'\Traversable' is already announced in '\AliasingCasesHolder\AbstractClass'.">ForeachSupport</error> {}
}