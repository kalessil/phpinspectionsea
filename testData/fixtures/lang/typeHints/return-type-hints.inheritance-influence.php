<?php

interface ExistingInterface {}

class ClassWithNonExistingInterface implements NonExistingInterface
{
    public function <weak_warning descr="[EA] ': void' can be declared as return type hint.">returnsVoid</weak_warning>() {
    }

    public function <weak_warning descr="[EA] ': string' can be declared as return type hint.">returnsString</weak_warning>() {
        return '';
    }

    public function <weak_warning descr="[EA] ': \stdClass' can be declared as return type hint.">returnsObject</weak_warning>() {
        return new stdClass();
    }
}

class ClassWithExistingInterface implements ExistingInterface
{
    public function <weak_warning descr="[EA] ': void' can be declared as return type hint.">returnsVoid</weak_warning>() {
    }

    public function <weak_warning descr="[EA] ': string' can be declared as return type hint.">returnsString</weak_warning>() {
        return '';
    }

    public function <weak_warning descr="[EA] ': \stdClass' can be declared as return type hint.">returnsObject</weak_warning>() {
        return new stdClass();
    }
}

