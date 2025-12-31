<?php

class SPSParent {
    public function method1($a) {
        return $a;
    }
    public function method2($a) {
        return $a;
    }
    public function method3($a = 1) {
        return $a;
    }

    /**
     * @psalm-pure
     */
    public function method4($a) {
        return $a;
    }
    public function method5($a) {
        return $a;
    }

    public function method6($a) {
        return $a;
    }
}

class SPSChild extends SPSParent {
    public function <weak_warning descr="[EA] The 'method1' method only calls its parent method, so it can be removed to simplify the code.">method1</weak_warning>($b) {
        parent::method1($b);
    }
    public function method2($a = null) { // nullable behaviour modified
        parent::method2($a);
    }
    public function method3($a = []) {   // default value modified
        parent::method3($a);
    }

    /**
     * @psalm-pure
     */
    public function <weak_warning descr="[EA] The 'method4' method only calls its parent method, so it can be removed to simplify the code.">method4</weak_warning>($a) {
        parent::method4($a);
    }
    /**
     * @psalm-pure
     */
    public function method5($a) {
        parent::method5($a);
    }

    public function method6(#[Autowire] $a) {
        parent::method6($a);
    }
}