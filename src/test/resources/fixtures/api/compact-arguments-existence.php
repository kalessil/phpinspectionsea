<?php

function buggyCompact($x, $y)
{
    $z = $x + $y;
    return
        <error descr="$zz might not be defined in the scope.">compact</error> ('x', 'y', 'z', 'zz', 'zz', "$$z")
        +
        <error descr="$$zz might not be defined in the scope.">compact</error>('x', 'y', 'z', '$zz', '$zz', "$$z")
    ;
}