<?php

class CasesHolder
{
    public function interfaceVariables($x): array
    {
        $a = function ($y) use ($x) {
            return $x ?? $y;
        };
        $b = function ($y) use ($x) {
            return $y ?? $x;
        };
        return [$a, $b];
    }

    public function reportedCases()
    {
        $x = <error descr="'$x' seems to be not defined in the scope.">$x</error> ?? '';
        $z = <error descr="'$y' seems to be not defined in the scope.">$y</error> ?? '';
        return [$x, $z];
    }

    public function relyOnIde() {
        echo $x;
        echo $x ?? '';
    }
}