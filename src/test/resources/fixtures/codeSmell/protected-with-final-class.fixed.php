<?php

class PropertyClass1 {
    public $isPublic;
    protected $isProtected;
    private $isPrivate;
}

final class PropertyClass2 {
    public $isPublic;
    private $isProtected;
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
    private function method2() { }
    private function method3() { }

    static function method0s() { }
    static public function method1s() { }
    static private function method2s() { }
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
    private const CONST2 = 2;
    private const CONST3 = 3;
}

final class ConstClass3 {
    private const IGNORE_CASE = true;
}
