<?php

    require_once <weak_warning descr="Unnecessary parentheses.">('whatever.php')</weak_warning>;
    $x = <weak_warning descr="Unnecessary parentheses.">($x)</weak_warning> + 1;
    $x = <weak_warning descr="Unnecessary parentheses.">(file_get_contents())</weak_warning>;
    $x = clone<weak_warning descr="Unnecessary parentheses.">($object)</weak_warning>;
    foreach ([] as $value) {
        continue<weak_warning descr="Unnecessary parentheses.">(2)</weak_warning>;
        break<weak_warning descr="Unnecessary parentheses.">(2)</weak_warning>;
    }

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
    ($x ?? '...')();

    $x = !($first = $x->prop)->with('data');

    $x = <weak_warning descr="Unnecessary parentheses.">(!empty($x))</weak_warning>;
    $x = <weak_warning descr="Unnecessary parentheses.">(!isset($x))</weak_warning>;

    $object = new stdClass();
    (function() { })->call($object);

    $x = (/** PhpDoc */ $x = $object) instanceof \Clazz;

    function yield_constructs($object) {
        return (yield $object->method()) !== false;
    }