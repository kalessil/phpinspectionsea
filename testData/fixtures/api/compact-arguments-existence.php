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
            <error descr="'$zz' might not be defined in the scope.">'zz'</error>,
            "$$z"
        )
        +
        compact(
            'x',
            'y',
            'z',
            '$zz',
            <error descr="'$$zz' might not be defined in the scope.">'$zz'</error>,
            "$$z"
        )
    ;
}

function buggyCompactControlFlow($x, $y)
{
    $temp = compact(
        'x',
        'y',
        <error descr="'$z' might not be defined in the scope.">'z'</error>
    );
    $z = $x + $y;

    return $temp;
}

function variable_variables() {
    $value = 'value';
    $name = 'value';

    $reported = 'reported';

    return compact($name, <weak_warning descr="There is chance that it should be 'reported' here.">$reported</weak_warning>);
}