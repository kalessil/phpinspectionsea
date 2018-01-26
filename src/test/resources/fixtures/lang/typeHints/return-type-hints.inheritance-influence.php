<?php

interface ExistingInterface {}

class ClassWithNonExistingInterface implements NonExistingInterface
{
    public function <weak_warning descr="': void' can be declared as return type hint.">returnsVoid</weak_warning>() {
    }

    public function <weak_warning descr="': string' can be declared as return type hint.">returnsString</weak_warning>() {
        return '';
    }
}

class ClassWithExistingInterface implements ExistingInterface
{
    public function <weak_warning descr="': void' can be declared as return type hint.">returnsVoid</weak_warning>() {
    }

    public function <weak_warning descr="': string' can be declared as return type hint.">returnsString</weak_warning>() {
        return '';
    }
}

