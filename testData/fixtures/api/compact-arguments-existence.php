<?php

function buggyCompact($x, $y)
{
    $z = $x + $y;
    return
        compact (
            'x',
            'y',
            'z',
            'zz',
            <error descr="[EA] '$zz' might not be defined in the scope.">'zz'</error>,
            "$$z"
        )
        +
        compact(
            'x',
            'y',
            'z',
            '$zz',
            <error descr="[EA] '$$zz' might not be defined in the scope.">'$zz'</error>,
            "$$z"
        )
    ;
}

function buggyCompactControlFlow($x, $y)
{
    $temp = compact(
        'x',
        'y',
        <error descr="[EA] '$z' might not be defined in the scope.">'z'</error>
    );
    $z = $x + $y;

    return $temp;
}