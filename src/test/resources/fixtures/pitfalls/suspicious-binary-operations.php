<?php

/* a pitfall: instanceof <trait> returns false */
trait IOAT_Trait {}
$x = <error descr="instanceof against traits returns 'false'.">$z instanceof IOAT_Trait</error>;
$x = <error descr="instanceof against traits returns 'false'.">$z instanceof \IOAT_Trait</error>;

/* false-positive: instanceof in trait and late binding */
trait IOAT_InTrait {
    public function a($x){ return $x instanceof self;   }
    public function b($x){ return $x instanceof static; }
    public function c($x){ return $x instanceof $this;  }
}

/* a bug: left and right operands are identical */
$a = <error descr="Left and right operands are identical.">$x == ($x)</error>;
$a = <error descr="Left and right operands are identical.">($x) == $x</error>;
$a = <error descr="Left and right operands are identical.">$x == $x</error>;

/* a typo: comparison instead of assignment */
$a <error descr="It seems that '=' should be here.">==</error> $b;

/* a typo: greater or equal instead of has element definition */
$a = ['x' <error descr="It seems that '=>' should be here.">>=</error> 'y'];
$a = [$x >= 'y']; // <- left operand is not a string, hence not reported

/* a bug: misplaced operator */
class MisplacedOperations
{
    public function method(bool $one) {
        if (count($one <error descr="This operator is probably misplaced.">></error> 0)) {}

        if ($this->method($one > 0)) {}
    }
}

/* a bug: hardcoded booleans */
$x = (
    <error descr="This boolean makes no sense or enforces the operation result.">true</error> ||
    <error descr="This boolean makes no sense or enforces the operation result.">true</error> ||
    false === true
);

/* a bug: ternary always returns the argument */
$y = [
    <error descr="The operation results to '(int)$x', please add missing parenthesises.">(int)$x</error> ?? 'whatever',
    (<error descr="The operation results to '(string)$x', please add missing parenthesises.">(string)$x</error>) ?? 'whatever'
];

/* operations priority issues */
if (<error descr="Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a = $b !== $c</error>) {}
if ($a || <error descr="Operations priority might differ from what you expect: please wrap needed with '(...)'.">$b && $c</error>) {}
if (<error descr="Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a && $b</error> || $c) {}
if ($a = <error descr="Operations priority might differ from what you expect: please wrap needed with '(...)'.">function1() && $b</error>) {}
if ($a = <error descr="Operations priority might differ from what you expect: please wrap needed with '(...)'.">function1() && $b = function2()</error>) {}
if ($a = <error descr="Operations priority might differ from what you expect: please wrap needed with '(...)'.">function1() && $b && $c = function2()</error>) {}
if ($a || ($b && $c)) {}
if ($a && ($b || $c)) {}
$z = $x && $b;
