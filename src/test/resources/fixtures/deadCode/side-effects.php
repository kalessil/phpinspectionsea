<?php

sideEffectUnknow();

function sideEffectNone($number = null) {
}

<weak_warning descr="This call can be removed because it have no side-effect.">sideEffectNone();</weak_warning>
<weak_warning descr="This call can be removed because it have no side-effect.">sideEffectNone(1);</weak_warning>
