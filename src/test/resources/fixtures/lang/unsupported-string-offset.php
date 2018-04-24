<?php

/** @param string[] $strings */
function cases_holder(string $string, array $strings)
{
    $one = $string[0][0];
    $one = [$string[0][0]];
    $string[0] = 0;

    <error descr="Could provoke a PHP Fatal error (cannot use string offset as an array).">$string[0][0]</error> = 0;

    <error descr="Could provoke a PHP Fatal error (cannot use string offset as an array).">$string[0]['...']</error>
        = <error descr="Could provoke a PHP Fatal error (cannot use string offset as an array).">$string[1]['...']</error>
        = 0;

    list(<error descr="Could provoke a PHP Fatal error (cannot use string offset as an array).">$string[0]['...']</error>) = $one;


    <error descr="Could provoke a PHP Fatal error ([] operator not supported for strings).">$string[]</error> = '';
    <error descr="Could provoke a PHP Fatal error ([] operator not supported for strings).">$strings[0][]</error> = '';
}

/* global context is not checked */
$string       = '...';
$string[0]    = 'a';
$string[0][0] = 'a';