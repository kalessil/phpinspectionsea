<?php

    $x = array();

    echo isset($x[0]) ? $x[0] : null;
    echo isset($x[0]) ? $x[0] : 'default';
    echo array_key_exists(0, $x) ? $x[0] : 'default';

    echo isset($x[0]) ? $x[0]->x : 'default';
    echo isset($x[0], $x[0]) ? $x[0] : 'default';