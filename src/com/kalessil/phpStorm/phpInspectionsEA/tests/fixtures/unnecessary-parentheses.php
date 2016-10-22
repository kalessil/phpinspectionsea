<?php

    require_once <weak_warning descr="Unnecessary parentheses">('whatever.php')</weak_warning>;
    $x = <weak_warning descr="Unnecessary parentheses">($x)</weak_warning> + 1;


    $x = ($x + 1) / 2;

    (new stdClass())->with('data');
    (clone $stdClass)->with('data');

    ($x->callableProperty)();
    ($x->getCallableProperty())();

    $mixedIncludeReturn = (include __DIR__ . '/foo.php');


