<?php

    $x = $y ? <weak_warning descr="' ... ?: ...' construction shall be used instead">$y</weak_warning> : 0;
    $x = $y ? <weak_warning descr="' ... ?: ...' construction shall be used instead">(($y))</weak_warning> : 0;
    $x = ($y) ? <weak_warning descr="' ... ?: ...' construction shall be used instead">$y</weak_warning> : 0;
    $x = ((($y))) ? <weak_warning descr="' ... ?: ...' construction shall be used instead">($y)</weak_warning> : 0;

    /* false-positives */
    $x = $y ? 0 : $y;
    $x = !$y ? $y : 0;