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

    public function reportArrayAccessCases() {
        $x = <error descr="'$x' seems to be not defined in the scope.">$x</error>[0][0] ?? null;
        $e = empty(<error descr="'$e' seems to be not defined in the scope.">$e</error>[0][0]);
        $i = isset(<error descr="'$i' seems to be not defined in the scope.">$i</error>[0][0]);

        return [ $x, $e, $i ];
    }

    public function reportMemberReferencesCases() {
        $x = <error descr="'$x' seems to be not defined in the scope.">$x</error>->property ?? null;
        $e = empty(<error descr="'$e' seems to be not defined in the scope.">$e</error>->property);
        $i = isset(<error descr="'$i' seems to be not defined in the scope.">$i</error>->method()[0]);

        return [ $x, $e, $i ];
    }

    public function loops() {
        while (true) {
            $something = isset($variableInWhile) ? $variableInWhile : '...';
            $variableInWhile = '...';
        }

        do {
            $something = isset($variableInDo) ? $variableInDo : '...';
        } while ($variableInDo = '...');
    }

    public function withInclude() {
        require '...';
        return isset($variable);
    }
}