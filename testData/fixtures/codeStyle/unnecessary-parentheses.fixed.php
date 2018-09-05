<?php

    require_once 'whatever.php';
    $x = $x + 1;
    $x = file_get_contents();
    $x = clone $object;
    foreach ([] as $value) {
        continue 2;
        break 2;
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

    $x = !empty($x);
    $x = !isset($x);

    $object = new stdClass();
    (function() { })->call($object);

    $x = (/** PhpDoc */ $x = $object) instanceof \Clazz;

    function yield_constructs($object) {
        return (yield $object->method()) !== false;
    }