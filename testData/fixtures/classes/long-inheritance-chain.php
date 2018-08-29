<?php

namespace {
    class Level1Class                     {}
    class Level2Class extends Level1Class {}
    class Level3Class extends Level2Class {}

    class <weak_warning descr="Class has 3 parent classes, consider using appropriate design patterns.">Level4Class</weak_warning>
                      extends Level3Class {}
}

namespace PHPUnit\Framework {
    class TestCase                                        {}
    class AbstractTestCase       extends TestCase         {}
    class TestCaseImplementation extends AbstractTestCase {}
    class <weak_warning descr="Class has 3 parent classes, consider using appropriate design patterns.">IDoTestingWrong</weak_warning>
                                 extends TestCaseImplementation  {}
}
namespace yii\base {
    class Behavior                                        {}
    class AbstractBehavior       extends Behavior         {}
    class BehaviorImplementation extends AbstractBehavior {}
    class <weak_warning descr="Class has 3 parent classes, consider using appropriate design patterns.">IDoOopWrong</weak_warning>
                                 extends BehaviorImplementation  {}
}
namespace Zend\Form {
    class Form                                        {}
    class AbstractForm           extends Form         {}
    class FormImplementation     extends AbstractForm {}
    class <weak_warning descr="Class has 3 parent classes, consider using appropriate design patterns.">IDoOopWrong</weak_warning>
                                 extends FormImplementation  {}
}

namespace Phalcon\Di {
    class Injectable                          {}
}
namespace Phalcon\Forms {
    class Form extends \Phalcon\Di\Injectable {}
    class FormImplementation     extends Form {}
    class <weak_warning descr="Class has 3 parent classes, consider using appropriate design patterns.">IDoOopWrong</weak_warning>
                                 extends FormImplementation  {}
}