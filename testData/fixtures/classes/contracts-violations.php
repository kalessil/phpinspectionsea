<?php

interface Contract {
    public function contractMethod();
}
abstract class AbstractClass {
    use IndirectMixin;
    abstract public function classMethod();
}
trait IndirectMixin {
    public function indirectTraitMethod() {}
}
trait DirectMixin {
    public function directTraitMethod() {}
}

/** @method annotatedMethod() */
class <warning descr="[EA] Some of public methods (ownPublicMethod, ownPublicStaticMethod) are not part of the class contracts. Perhaps a contract is incomplete.">ImplementationClass</warning> extends AbstractClass implements Contract {
    use DirectMixin;
    public function __construct() {}

    public function indirectTraitMethod() {}
    public function directTraitMethod() {}

    public function contractMethod() {}
    public function classMethod()    {}

    public function ownPublicMethod()       {}
    protected function ownProtectedMethod() {}
    private function ownPrivateMethod()     {}

    public static function ownPublicStaticMethod()       {}
    protected static function ownProtectedStaticMethod() {}
    private static function ownPrivateStaticMethod()     {}
}

class DetailedException extends \DomainException {
    public function withMoreDetails() {}
}