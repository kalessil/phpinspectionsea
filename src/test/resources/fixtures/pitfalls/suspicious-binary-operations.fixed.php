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

/* a typo: comparison instead of assignment */
$a == $b;

/* a typo: greater or equal instead of has element definition */
$a = ['x' >= 'y'];
$a = [$x >= 'y']; // <- left operand is not a string, hence not reported

/* a bug: misplaced operator */
class MisplacedOperations
{
    public function method(bool $one) {
        if (count($one) > 0) {}

        if ($this->method($one > 0)) {}
    }
}

/* a bug: hardcoded booleans */
$x = (
    true ||
    false ||
    null ||
    false === true
);

/* a bug: ternary always returns the argument */
$y = [
    (int)$x ?? '...',
    ((string)$x) ?? '...',
    (!$x) ?? '...',
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
if ((!($a)) > $b) {}
if ((!$a) > $b) {}

/* nullable/falsy values comparison cases */
$nullable = null;
$falsy    = false;
if ($nullable >= 5) {}
if ($falsy > 5) {}

/* logical operands and multi-value cases */
if ($x == 5 && $x == 6) {}
if (5 === $x && 6 === $x) {}
if ($x != 'x' || $x != 'y') {}
if ('x' !== $x || 'y' !== $x) {}

/* false-positives: operators not the same, complex expressions */
if ($x == 5 && $x === 6) {}
if ($x != 'x' || $x !== 'y') {}
if ($x == 5 && $x == $y) {}

/* typos in logical operands */
if ($x && $x & $x) {}
if ($x || $x & $x) {}
if ($x && $x | $x) {}
if ($x || $x | $x) {}

/* false-positives: parentheses, mixed operators, integer types */
if ($x && ($x & $x)) {}
if ($x || ($x & $x)) {}
if ($x || ($x | $x)) {}
if ($x && ($x | $x)) {}
if ($x && 10 & 5) {}
if ($x || 10 | 5) {}