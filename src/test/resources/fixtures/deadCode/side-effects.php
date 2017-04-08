<?php

sideEffectUnknow();

function sideEffectNone($number = null) {
}

<weak_warning descr="This call can be removed because it have no side-effect.">sideEffectNone();</weak_warning>
<weak_warning descr="This call can be removed because it have no side-effect.">sideEffectNone(1);</weak_warning>

function sideEffectPossible($arg1, &$arg2 = null) {
}

<weak_warning descr="This call can be removed because it have no side-effect.">sideEffectPossible(1);</weak_warning>
sideEffectPossible(1, $arg2);
