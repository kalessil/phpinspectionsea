<?php

class Exception1 extends \Exception {}
class Exception2 extends \Exception {}
class Exception3 extends \Exception {}

/* transitive, kept in a trait */
interface _Interface {
    /** @throws \Exception1 */
    public function method();
}
abstract class _ClassWithInterface implements _Interface {
    /** @inheritdoc */
    public function method() {}
}

/* transitive, kept in a trait */
trait _Trait {
    /** @throws \Exception2 */
    public function method() {}
}
abstract class _ClassWithTrait implements _Interface {
    use _Trait;
}

class CasesHolder {
    /**
     * PhpDoc with missing exceptions annotation
     */
    public function one(_ClassWithInterface $one, _ClassWithTrait $two) {
        <weak_warning descr="Throws a non-annotated/unhandled exception: '\Exception1'.">$one->method()</weak_warning>;
        <weak_warning descr="Throws a non-annotated/unhandled exception: '\Exception2'.">$two->method()</weak_warning>;
    }

    /**
     * PhpDoc with missing exceptions annotation
     * @throws \Exception1
     */
    public function two(_ClassWithInterface $one, _ClassWithTrait $two) {
        $one->method();
        <weak_warning descr="Throws a non-annotated/unhandled exception: '\Exception2'.">$two->method()</weak_warning>;
    }
}