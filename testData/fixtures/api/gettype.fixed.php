<?php
    $x = gettype($x) === 'unknown type';
    $x = gettype($x) === 'resource (closed)';

    /* incorrect type checked */
    $x = gettype($x) === 'oops';
    /* aliasing taken into account */
    $x = is_float($x);
    $x = is_int($x);

    /* all functions handled correctly */
    $x = is_bool($x);
    $x = is_int($x);
    $x = is_float($x);
    $x = is_string($x);
    $x = is_array($x);
    $x = is_object($x);
    $x = is_resource($x);
    $x = is_null($x);

    /* comparison operands are handled correctly */
    $x = is_string($x);
    $x = !is_string($x);
    $x = is_string($x);
    $x = !is_string($x);
    $x = is_string($x);
    $x = !is_string($x);
    $x = is_string($x);
    $x = !is_string($x);