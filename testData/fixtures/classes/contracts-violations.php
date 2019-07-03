<?php

interface Contract {
    public function contractMethod();
}
abstract class AbstractClass {
    abstract public function classMethod();
}
class ImplementationClass extends AbstractClass implements Contract {
    public function contractMethod() {}
    public function classMethod()    {}

    public function ownPublicMethod()       {}
    protected function ownProtectedMethod() {}
    private function ownPrivateMethod()     {}
}