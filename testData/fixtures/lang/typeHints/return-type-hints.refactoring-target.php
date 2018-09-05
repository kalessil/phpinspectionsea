<?php


interface ExistingInterface {
    function methodWithUndeclaredType();
    function methodWithStringType(): string;
}

class ImplementsInterface implements ExistingInterface {
    function <weak_warning descr="': void' can be declared as return type hint (please use change signature intention to fix this).">methodWithUndeclaredType</weak_warning>() { return; }
    function <weak_warning descr="': string' can be declared as return type hint (please use change signature intention to fix this).">methodWithStringType</weak_warning>() { return ''; }
}

class ExistingParent {
    abstract function methodWithUndeclaredType();
    abstract function methodWithStringType(): string;
}

class ImplementsAbstractMethods extends ExistingParent {
    function <weak_warning descr="': void' can be declared as return type hint (please use change signature intention to fix this).">methodWithUndeclaredType</weak_warning>() { return; }
    function <weak_warning descr="': string' can be declared as return type hint (please use change signature intention to fix this).">methodWithStringType</weak_warning>() { return ''; }
}


