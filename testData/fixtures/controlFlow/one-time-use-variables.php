<?php

<warning descr="[EA] Variable $x is redundant.">$x</warning> = 1;
return $x;

<warning descr="[EA] Variable $x is redundant.">$x</warning> = returnByReference();
return $x->x;

<warning descr="[EA] Variable $x is redundant.">$x</warning> = new \stdClass();
return $x->x;

<warning descr="[EA] Variable $x is redundant.">$x</warning> = clone $x;
return $x->x;

<warning descr="[EA] Variable $y is redundant.">$y</warning> = new Exception();
throw $y;

function list_unpack()
{
    <warning descr="[EA] Variable $list is redundant.">$list</warning> = array(1, 2);
    list($a, $b) = $list;
    return $a + $b;
}

function array_assembling()
{
    $filters = ['is_email_compatible' => 1];
    return ['widget_filters' => $filters];
}

function quick_fixing($argument, $alternative) {
    <warning descr="[EA] Variable $ternary is redundant.">$ternary</warning> = $argument ?? $alternative;
    return $ternary->property;

    <warning descr="[EA] Variable $coalescing is redundant.">$coalescing</warning> = $argument ?: $alternative;
    return $coalescing->property;
}