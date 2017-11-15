<?php

    require_once <weak_warning descr="Unnecessary parentheses.">('whatever.php')</weak_warning>;
    $x = <weak_warning descr="Unnecessary parentheses.">($x)</weak_warning> + 1;
    $x = <weak_warning descr="Unnecessary parentheses.">(file_get_contents())</weak_warning>;
    $x = clone<weak_warning descr="Unnecessary parentheses.">($object)</weak_warning>;

    $x = ($x + 1) / 2;
    $x = ((array) $x)[0];

    (new stdClass())->with('data');
    (new stdClass())->prop;

    ($x ?: $y)->with('data');
    ($x ?: $y)->prop;
    ($x ?? $y)->with('data');
    ($x ?? $y)->prop;

    (clone $stdClass)->with('data');
    (clone $stdClass)->prop;

    ($x->callableProperty)();
    ($x->getCallableProperty())();

    /* __invoke */
    (new stdClass())();
    (new self)();
    (clone $stdClass)();

    $mixedIncludeReturn = (include __DIR__ . '/foo.php');

    (function(){})();
    ($x = function(){})();
    ('Class'.'::method')();

    $x = !($first = $x->prop)->with('data');