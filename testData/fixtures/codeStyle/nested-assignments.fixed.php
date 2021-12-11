<?php

function cases_holder()
{
    $b = 0;
    $a = 0;

    $b = '';
    $a = '';

    $c = trim('');
    $b = $c;
    $a = $c;

    $b = $z;
    $a = $z;

    /* false-positives */
    $a = 0;
}