<?php

function cases_holder(string $string)
{
    $one = $string[0][0];
    $one = [ $string[0][0] ];
    $string[0] = 0;

    <error descr="Provokes a PHP Fatal error (cannot use string offset as an array).">$string[0][0]</error> = 0;

    <error descr="Provokes a PHP Fatal error (cannot use string offset as an array).">$string[0]['...']</error>
        = <error descr="Provokes a PHP Fatal error (cannot use string offset as an array).">$string[1]['...']</error>
        = 0;

    [<error descr="Provokes a PHP Fatal error (cannot use string offset as an array).">$string[0]['...']</error>]
        = ['...'];

    array(<error descr="Provokes a PHP Fatal error (cannot use string offset as an array).">$string[0]['...']</error>)
        = array('...', '...');
}