<?php

interface Contract {
    public function contractMethod();
}
abstract class AbstractClass {
    use Mixin;
    abstract public function classMethod();
}
trait Mixin {
    public function traitMethod() {}
}

/** @method annotatedMethod() */
class <warning descr="Some of public methods (ownPublicMethod) are not part of the class contracts. Perhaps a contract is incomplete.">ImplementationClass</warning> extends AbstractClass implements Contract {
    public function __construct() {}

    public function traitMethod() {}

    public function contractMethod() {}
    public function classMethod()    {}

    public function ownPublicMethod()       {}
    protected function ownProtectedMethod() {}
    private function ownPrivateMethod()     {}
}