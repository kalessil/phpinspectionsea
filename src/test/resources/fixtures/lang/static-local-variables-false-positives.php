<?php

class SLVRefClass {
    public function __construct(& $param) {}
}

class SLVFalsePositives {
   public function params ($param) {
        $param = [''];
        return $param;
   }

    private function ref (&$ref) {
        $passedByRef = [''];
        return $this->ref($passedByRef);

        $construct = [''];
        $obj = new SLVRefClass($construct);
    }

    public function func ($x) {
        $empty = [];
        return $empty;

        $withInjections = [$x];
        return $withInjections;

        static $SLV = [''];
        return $SLV;

        $overridden = [''];
        $overridden = $x;
        return $overridden;

        $unset = [''];
        unset($unset[0], $unset[0]);
        return $overridden;

        $referenced = [''];
        $x = &$referenced;
        return $x;
    }
}