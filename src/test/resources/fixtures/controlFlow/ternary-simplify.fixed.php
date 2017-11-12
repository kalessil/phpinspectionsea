<?php

    $x = $x > 0;
    $x = $x <= 0;
    $x = (bool)($x & 0);
    $x = !($x & 0);
    $x = ($x && $y);
    $x = !($x && $y);

    $x = $x > 0;
    $x = $x > 0;

    $x = $x > 0 ? true : null;
    $x = is_numeric($x) ? false : true;

    $x = call($x ? $x : null);
    $x = call($x ? $x : null, null);
    $x = Clazz::method($x ? $x : null);
    $x = $object->method($x ? $x : null);

    $x = $x ? call($x) : Clazz::call(null);
    $x = $x ? call($x, $y) : call(null, null);
    $x = $x ? call1($x, null) : call2(null, null);