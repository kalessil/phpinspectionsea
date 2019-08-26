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

/* a bug: hardcoded booleans */
$x = [
    $x && false,
    $x && null,
    $x && true,
    $x || false,
    $x || null,
    $x || true,
];

/* operations priority issues */
if ($a = ($b !== $c)) {}
if ($a || ($b && $c)) {}
if (($a && $b) || $c) {}
if ($a = (function1() && $b)) {}
if ($a = (function1() && $b = function2())) {}
if ($a = (function1() && $b && $c = function2())) {}
if ($a || ($b && $c)) {}
if ($a && ($b || $c)) {}
$z = $x && $b;
if ((!$a) > $b) {}
if ((!$a) == $b) {}
if ((!$a) === $b) {}
if ((!($a)) > $b) {}
if ((!$a) > $b) {}
if ((!$a) == $b) {}
if ((!$a) === $b) {}
if (!$a <=> $b) {}

/* operations priority issues: ternaries and null coalescing */
if ($a ?: $b && $c) {}
if ($a ?: $b || $c) {}
if ($a ?? ($b && $c)) {}
if ($a ?? ($b || $c)) {}

/* nullable/falsy values comparison cases */
$nullable = null;
$falsy    = false;
if ($nullable >= 5) {}
if ($falsy > 5) {}

/* logical operands and multi-value cases */
if ($x == 5 && $x == 6) {}
if ($x == 5 && $x === 6) {}
if ($x === 5 && $x === 6) {}
if ($x != 5 || $x != 6) {}
if ($x != 5 || $x !== 6) {}
if ($x !== 5 || $x !== 6) {}
if ($x == 5 || $x != 6) {}
if ($x == 5 && $x != 6) {}


/* false-positives: non-constant values */
if ($x == 5 && $x == $y) {}