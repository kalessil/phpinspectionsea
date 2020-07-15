<?php

/* a pitfall: instanceof <trait> returns false */
trait IOAT_Trait {}
$x = $z instanceof IOAT_Trait;
$x = $z instanceof \IOAT_Trait;

/* false-positive: instanceof in trait and late binding */
trait IOAT_InTrait {
    public function a($x){ return $x instanceof self;   }
    public function b($x){ return $x instanceof static; }
    public function c($x){ return $x instanceof $this;  }
}

/* a bug: left and right operands are identical */
$a = $x == ($x);
$a = ($x) == $x;
$a = $x == $x;

/* a bug: misplaced operator */
class MisplacedOperations
{
    public function method(bool $one) {
        if (count($one) > 0) {}

        if ($this->method($one > 0)) {}
    }
}

/* a bug: hardcoded booleans and null */
$x = [
    $x && false,
    $x && null,
    $x && true,
    $x || false,
    $x || null,
    $x || true,
];

/* operations priority issues: mixing || and && */
if ($a || ($b && $c)) {}
if (($a && $b) || $c) {}
if ($a || ($b && $c)) {}
if ($a && ($b || $c)) {}
if ($a || $b || $c) {}
if ($a && $b && $c) {}

/* operations priority issues: assignment */
if ($a = (function1() && $b)) {}
if ($a = (function1() && $b = function2())) {}
if ($a = (function1() && $b && $c = function2())) {}
if ($a = ($b !== $c)) {}
$z = $x && $b;

/* operations priority issues: inversion */
if ((!$a) > $b) {}
if ((!$a) == $b) {}
if ((!$a) === $b) {}
if ((!($a)) > $b) {}
if ((!$a) > $b) {}
if ((!$a) == $b) {}
if ((!$a) === $b) {}
if (!$a <=> $b) {}

/* operations priority issues: ternaries and null coalescing */
if ($a ?: ($b && $c)) {}
if ($a ?: ($b || $c)) {}
if ($a ?? ($b && $c)) {}
if ($a ?? ($b || $c)) {}
if ($x ?: ($y ?? $z)) {}
if ($x ?? ($y ?: $z)) {}

/* operations priority issues: ternaries and literal opeands */
echo ($a & $b) ? 0 : 1;
echo ($a | $b) ? 0 : 1;
echo ($a - $b) ? 0 : 1;
echo ($a + $b) ? 0 : 1;
echo ($a / $b) ? 0 : 1;
echo ($a * $b) ? 0 : 1;
echo ($a % $b) ? 0 : 1;
echo ($a ^ $b) ? 0 : 1;
echo $a and ($b ? 0 : 1);
echo $a or ($b ? 0 : 1);
echo $a xor ($b ? 0 : 1);
/* false-positives: condition is specified well */
echo ($a + $b) ? 0 : 1;
/* false-positives: conditions intentions are clear */
echo $a > $b ? 0 : 1;
echo $a >= $b ? 0 : 1;
echo $a < $b ? 0 : 1;
echo $a <= $b ? 0 : 1;
echo $a && $b ? 0 : 1;
echo $a || $b ? 0 : 1;
echo $a == $b ? 0 : 1;
echo $a != $b ? 0 : 1;
echo $a === $b ? 0 : 1;
echo $a !== $b ? 0 : 1;
echo $a instanceof stdClass ? 0 : 1;
echo $a <=> $b ? 0 : 1;

/* nullable/falsy values comparison cases */
$nullable = null;
$falsy    = false;
if ($nullable >= 5) {}
if ($falsy > 5) {}

