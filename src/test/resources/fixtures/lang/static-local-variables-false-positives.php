<?php

class SLVFalsePositives {
   public function __construct(& $param) {}

   public function params ($param) {
        $param = [''];
        return $param;
   }

    private function ref (&$ref) {
        $passedByRef = [''];
        echo $this->ref($passedByRef);

        $construct = [''];
        $obj = new SLVFalsePositives($construct);
    }

    public function func ($x) {
        $empty = [];
        echo $empty;

        $withInjections = [$x];
        echo $withInjections;

        static $SLV = [''];
        echo $SLV;

        $duplicates = [''];
        $duplicates = [''];
        echo $duplicates;

        $overridden = [''];
        $overridden = $x;
        echo $overridden;

        $unset = [''];
        unset($unset[0], $unset[0]);
        echo $overridden;

        $referenced = [''];
        $x = & $referenced;
        echo $x;

        $usedInLambda = [''];
        $lambda = function () use (&$usedInLambda) {};
    }
}