<?php

class ValidFactory1 {
    protected function __construct() {}
    public function createFromInt()  {}
}
class ValidFactory2 {
    protected function __construct() {}
    public function fromInt()        {}
}

class <warning descr="Ensure that one of public getInstance/create*/from* methods are defined.">ProtectedConstructorInFactor</warning> {
    protected function __construct() {}
}
