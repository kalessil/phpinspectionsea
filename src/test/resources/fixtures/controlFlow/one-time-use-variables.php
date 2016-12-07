<?php

<warning descr="Variable $x is redundant">$x</warning> = 1;
return $x;

<warning descr="Variable $x is redundant">$x</warning> = returnByReference();
return $x->x;

<warning descr="Variable $y is redundant">$y</warning> = new Exception();
throw $y;

function listUnpack () {
    <warning descr="Variable $list is redundant">$list</warning> = array(1, 2);
    list($a, $b) = $list;

    return $a + $b;
}