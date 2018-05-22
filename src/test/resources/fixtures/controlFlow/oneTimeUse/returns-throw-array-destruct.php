<?php

<warning descr="Variable $x is redundant.">$x</warning> = 1;
return $x;

<warning descr="Variable $x is redundant.">$x</warning> = returnByReference();
return $x->x;

<warning descr="Variable $x is redundant.">$x</warning> = new \stdClass();
return $x->x;

<warning descr="Variable $x is redundant.">$x</warning> = clone $x;
return $x->x;

<warning descr="Variable $y is redundant.">$y</warning> = new Exception();
throw $y;

function list_unpack()
{
    <warning descr="Variable $list is redundant.">$list</warning> = array(1, 2);
    list($a, $b) = $list;
    return $a + $b;
}

function array_assembly()
{
    $filters = ['is_email_compatible' => 1];
    return ['widget_filters' => $filters];
}

function assignment() {
    <warning descr="Variable $temporary is redundant.">$temporary</warning> = '...';
    $result = $temporary;
    return trim($result);
}