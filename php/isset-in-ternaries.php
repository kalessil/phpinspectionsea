<?php

    $x = null;

    echo isset($x) ? $x : null;
    echo !isset($x) ? null : $x;

    echo isset($x) ? isset($x) : null;
    echo !isset($x) ? null : isset($x);