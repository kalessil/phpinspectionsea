<?php

/* a pitfall: instanceof <trait> returns false */
trait IOAT_Trait {}
$x = <error descr="[EA] instanceof against traits returns 'false'.">$z instanceof IOAT_Trait</error>;
$x = <error descr="[EA] instanceof against traits returns 'false'.">$z instanceof \IOAT_Trait</error>;

/* false-positive: instanceof in trait and late binding */
trait IOAT_InTrait {
    public function a($x){ return $x instanceof self;   }
    public function b($x){ return $x instanceof static; }
    public function c($x){ return $x instanceof $this;  }
}

/* a bug: left and right operands are identical */
$a = <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x == ($x)</error>;
$a = <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">($x) == $x</error>;
$a = <error descr="[EA] Same value used in the operation (the operation is incorrect or can be simplified).">$x == $x</error>;

/* a bug: misplaced operator */
class MisplacedOperations
{
    public function method(bool $one) {
        if (count($one <error descr="[EA] This operator is probably misplaced.">></error> 0)) {}

        if ($this->method($one > 0)) {}
    }
}

/* a bug: hardcoded booleans and null */
$x = [
    $x && <error descr="[EA] This operand enforces the operation result.">false</error>,
    $x && <error descr="[EA] This operand enforces the operation result.">null</error>,
    $x && <error descr="[EA] This operand doesn't make any sense here.">true</error>,
    $x || <error descr="[EA] This operand doesn't make any sense here.">false</error>,
    $x || <error descr="[EA] This operand doesn't make any sense here.">null</error>,
    $x || <error descr="[EA] This operand enforces the operation result.">true</error>,
];

/* operations priority issues: mixing || and && */
if ($a || <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$b && $c</error>) {}
if (<error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a && $b</error> || $c) {}
if ($a || ($b && $c)) {}
if ($a && ($b || $c)) {}
if ($a || $b || $c) {}
if ($a && $b && $c) {}

/* operations priority issues: assignment */
if ($a = <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">function1() && $b</error>) {}
if ($a = <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">function1() && $b = function2()</error>) {}
if ($a = <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">function1() && $b && $c = function2()</error>) {}
if (<error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a = $b !== $c</error>) {}
$z = $x && $b;

/* operations priority issues: inversion */
if (<error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">! $a > $b</error>) {}
if (<error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">! $a == $b</error>) {}
if (<error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">! $a === $b</error>) {}
if (<error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">! ($a) > $b</error>) {}
if ((!$a) > $b) {}
if ((!$a) == $b) {}
if ((!$a) === $b) {}
if (!$a <=> $b) {}

/* operations priority issues: ternaries and null coalescing */
if ($a ?: <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$b && $c</error>) {}
if ($a ?: <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$b || $c</error>) {}
if ($a ?? <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$b && $c</error>) {}
if ($a ?? <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$b || $c</error>) {}
if ($x ?: <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$y ?? $z</error>) {}
if ($x ?? <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$y ?: $z</error>) {}

/* operations priority issues: ternaries and literal opeands */
echo <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a & $b</error> ? 0 : 1;
echo <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a | $b</error> ? 0 : 1;
echo <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a - $b</error> ? 0 : 1;
echo <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a + $b</error> ? 0 : 1;
echo <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a / $b</error> ? 0 : 1;
echo <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a * $b</error> ? 0 : 1;
echo <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a % $b</error> ? 0 : 1;
echo <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$a ^ $b</error> ? 0 : 1;
echo $a and <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$b ? 0 : 1</error>;
echo $a or <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$b ? 0 : 1</error>;
echo $a xor <error descr="[EA] Operations priority might differ from what you expect: please wrap needed with '(...)'.">$b ? 0 : 1</error>;
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
if (<error descr="[EA] This might work not as expected (an argument can be null/false), use '$nullable >= 5' to be sure.">!($nullable < 5)</error>) {}
if (<error descr="[EA] This might work not as expected (an argument can be null/false), use '$falsy > 5' to be sure.">!((($falsy <= 5)))</error>) {}

