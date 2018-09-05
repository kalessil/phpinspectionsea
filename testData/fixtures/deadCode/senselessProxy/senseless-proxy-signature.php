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
}

class SPSChild extends SPSParent {
    public function <weak_warning descr="'method1' method can be dropped, as it only calls parent's one.">method1</weak_warning>($b) {
        parent::method1($b);
    }
    public function method2($a = null) { // nullable behaviour modified
        parent::method2($a);
    }
    public function method3($a = []) {   // default value modified
        parent::method2($a);
    }
}