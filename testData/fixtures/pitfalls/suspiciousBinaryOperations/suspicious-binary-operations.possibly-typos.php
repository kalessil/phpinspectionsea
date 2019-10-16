<?php


/* a typo: comparison instead of assignment */
$a <error descr="[EA] It seems that '=' should be here.">==</error> $b;

/* a typo: greater or equal instead of has element definition */
$a = ['x' <error descr="[EA] It seems that '=>' should be here.">>=</error> 'y'];
$a = [$x >= 'y']; // <- left operand is not a string, hence not reported

/* a typo: greater instead of method reference */
function collision() {}
class Clazz { public function collision() {} }
$a = (new Clazz())<error descr="[EA] It seems that '->' should be here.">></error>collision();
