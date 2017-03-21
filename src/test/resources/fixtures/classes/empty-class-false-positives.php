<?php

trait EmptyTrait           {}
interface EmptyInterface   {}
class EmptyClassWithTraits { use EmptyTrait; }

class EmptyException extends Exception {}

/** @deprecated */
class EmptyDeprecatedClass {}

class ClassWithAProperty {
    public $property;
}
class ClassWithAMethod {
    public function __construct() {}
}

abstract class AbstractClass                { public function method() {} }
class ExtendsAbstract extends AbstractClass {}