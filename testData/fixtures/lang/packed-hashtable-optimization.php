<?php

    $z = <weak_warning descr="[EA] Reordering keys in natural ascending order would enable array optimizations here.">[</weak_warning>
        1 => $x, 0 => $y, 2 => $z
    ];
    $z = <weak_warning descr="[EA] Reordering keys in natural ascending order would enable array optimizations here.">array</weak_warning>
        (1 => $x, 0 => $y, 2 => $z);
    $z = <weak_warning descr="[EA] Using integer keys would enable array optimizations here.">array</weak_warning>
        ('0' => '', '1' => null, '2' => null, '3' => '', '4' => null);

    /* false-positives */
    array();
    array('index' => $x, 1 => $y, 2 => $z);
    array('00' => $x, 1 => $y, 2 => $z);
    array(0 => $x, 1 => $y, 2 => $z);
    array(0 => $x, $i => $y, 2 => $z);
    array($i => $x, $i => $y, 2 => $z);