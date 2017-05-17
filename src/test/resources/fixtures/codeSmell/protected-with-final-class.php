<?php

class PropertyClass1 {
    public $isPublic;
    protected $isProtected;
    private $isPrivate;
}

final class PropertyClass2 {
    public $isPublic;
    <weak_warning descr="Protected modifier could be replaced by private.">protected</weak_warning> $isProtected;
    private $isPrivate;
}

class MethodClass1 {
    function method0() { }
    public function method1() { }
    protected function method2() { }
    private function method3() { }

    static function method0s() { }
    static public function method1s() { }
    static protected function method2s() { }
    static private function method3s() { }
}

abstract class MethodClass2 {
    abstract function method0();
    abstract public function method1();
    abstract protected function method2();
}

final class MethodClass3 {
    function method0() { }
    public function method1() { }
    <weak_warning descr="Protected modifier could be replaced by private.">protected</weak_warning> function method2() { }
    private function method3() { }

    static function method0s() { }
    static public function method1s() { }
    static <weak_warning descr="Protected modifier could be replaced by private.">protected</weak_warning> function method2s() { }
    static private function method3s() { }
}

class ConstClass1 {
    const CONST0 = 0;
    public const CONST1 = 1;
    protected const CONST2 = 2;
    private const CONST3 = 3;
}

final class ConstClass2 {
    const CONST0 = 0;
    public const CONST1 = 1;
    <weak_warning descr="Protected modifier could be replaced by private.">protected</weak_warning> const CONST2 = 2;
    private const CONST3 = 3;
}
