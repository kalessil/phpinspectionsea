<?php

class ClassWithAProperty { public $property; }
class ClassWithAMethod   { public function __construct() {} }

class <weak_warning descr="Class does not contain any properties or methods.">AnEmptyClass</weak_warning> {}

/* false-positives: empty interface */
interface EmptyInterface {}

/* false-positives: empty class with traits */
trait _Trait { public function method() {} }
class EmptyClassWithTrait { use _Trait; }

/* false-positives: exception */
class ExceptionInMyDomain extends Exception {}

/* false-positives: deprecated class */
/** @deprecated */
class EmptyDeprecatedClass {}

/* false-positives: extends abstract */
abstract class AbstractClass { public function method() {} }
class ExtendsAbstract extends AbstractClass {}