<?php

class SLVPatterns {
    public function f() {
        <weak_warning descr="Variable can be static: property or 'static $array = [...]' (compile-time initialization).">$array</weak_warning> = [''];
        return $array;
    }
}