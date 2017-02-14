<?php

    require_once <weak_warning descr="Unnecessary parentheses.">('whatever.php')</weak_warning>;
    $x = <weak_warning descr="Unnecessary parentheses.">($x)</weak_warning> + 1;
    $x = <weak_warning descr="Unnecessary parentheses.">(file_get_contents())</weak_warning>;

    $x = ($x + 1) / 2;

    (new stdClass())->with('data');
    (new stdClass())->prop;

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


