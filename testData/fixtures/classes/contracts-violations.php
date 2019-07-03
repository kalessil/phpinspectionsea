<?php

interface Contract {
    public function contractMethod();
}
abstract class AbstractClass {
    abstract public function classMethod();
}
class <warning descr="Some of public methods (ownPublicMethod) are not part of the class contracts. Perhaps a contract is incomplete.">ImplementationClass</warning> extends AbstractClass implements Contract {
    public function contractMethod() {}
    public function classMethod()    {}

    public function ownPublicMethod()       {}
    protected function ownProtectedMethod() {}
    private function ownPrivateMethod()     {}
}