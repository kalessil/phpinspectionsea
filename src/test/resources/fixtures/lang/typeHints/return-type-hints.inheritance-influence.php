<?php

interface ExistingInterface {}

class ClassWithNonExistingInterface implements NonExistingInterface
{
    public function <weak_warning descr="': void' can be declared as return type hint.">returnsVoid</weak_warning>() {
    }

    public function <weak_warning descr="': string' can be declared as return type hint.">returnsString</weak_warning>() {
        return '';
    }

    public function <weak_warning descr="': \stdClass' can be declared as return type hint.">returnsObject</weak_warning>() {
        return new stdClass();
    }
}

class ClassWithExistingInterface implements ExistingInterface
{
    public function <weak_warning descr="': void' can be declared as return type hint.">returnsVoid</weak_warning>() {
    }

    public function <weak_warning descr="': string' can be declared as return type hint.">returnsString</weak_warning>() {
        return '';
    }

    public function <weak_warning descr="': \stdClass' can be declared as return type hint.">returnsObject</weak_warning>() {
        return new stdClass();
    }
}

