<?php

/* a pitfall: instanceof <trait> returns false */
trait IOAT_Trait {}
$x = <error descr="instanceof against traits returns 'false'.">$z instanceof IOAT_Trait</error>;
$x = <error descr="instanceof against traits returns 'false'.">$z instanceof IOAT_Trait::class</error>;
$x = <error descr="instanceof against traits returns 'false'.">$z instanceof \IOAT_Trait</error>;

/* false-positive: instanceof in trait and late binding */
trait IOAT_InTrait {
    public function a(){ return $this instanceof self;   }
    public function b(){ return $this instanceof static; }
    public function c(){ return $this instanceof $this;  }
}

/* a bug: left and right operands are identical */
$a = <error descr="Left and right operands are identical.">$x == ($x)</error>;
$a = <error descr="Left and right operands are identical.">$x == $x</error>;

/* a typo: comparison instead of assignment */
$a <error descr="It seems that '=' should be here.">==</error> $b;

/* a typo: greater or equal instead of has element definition */
$a = ['x' <error descr="It seems that '=>' should be here.">>=</error> 'y'];
$a = [$x >= 'y']; // <- left operand is not a string, hence not reported