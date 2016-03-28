<?php

function buggyCompact($x, $y) {
    $z = $x + $y;
    return compact(
        'x', 'y', 'z',
        'zz',  // <- reported
        '$zz'  // <- reported
    );
}