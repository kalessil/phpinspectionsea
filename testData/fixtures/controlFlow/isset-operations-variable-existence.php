<?php

class CasesHolder
{
    public function variableVariables($x): array {
        return [ ${$x}, ${$x[0]}, ${$x->property} ];
    }

    public function interfaceVariables($x): array
    {
        $a = function ($y) use ($x) {
            return [ $x ?? $y, isset($x), empty($x) ];
        };
        $b = function ($y) use ($x) {
            return [ $y ?? $x, isset($y), empty($y) ];
        };
        return [$a, $b];
    }

    public function specialCases(): array
    {
        return [ $this ?? 'whatever', isset($this), empty($this) ];
    }

    public function reportedCases()
    {
        $x = <error descr="[EA] '$x' seems to be not defined in the scope.">$x</error> ?? '';
        $e = empty(<error descr="[EA] '$e' seems to be not defined in the scope.">$e</error>);
        $i = isset(<error descr="[EA] '$i' seems to be not defined in the scope.">$i</error>);
        $z = <error descr="[EA] '$y' seems to be not defined in the scope.">$y</error> ?? '';
        return [$x, $z, $e, $i];
    }

    public function relyOnIde() {
        echo $x;
        echo $x ?? '';
    }

    public function loopsFor() {
         for ( ; ; ) {
             $something = isset($variable) ? $variable : '...';
         }
     }

    public function loopsWhile() {
         while (true) {
            $something = isset($variable) ? $variable : '...';
            $variable = '...';
        }
    }

    public function loopsDoWhile() {
        do {
            $something = isset($variable) ? $variable : '...';
            $variable = '...';
        } while (true);
    }

    public function withGoto() {
        entry:
        $something = isset($variable) ? $variable : '...';
        $variable = '...';
        goto entry;
    }

    public function withInclude() {
        require '...';
        return isset($variable);
    }
}