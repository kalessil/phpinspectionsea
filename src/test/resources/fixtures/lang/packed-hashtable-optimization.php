<?php

    $z = <weak_warning descr="Reordering element by increasing keys would enable packed hashtable optimizations here">[</weak_warning>
        1 => $x, 0 => $y
    ];
    $z = <weak_warning descr="Reordering element by increasing keys would enable packed hashtable optimizations here">array</weak_warning>
        (1 => $x, 0 => $y);
    $z = <weak_warning descr="Using integer keys would enable packed hashtable optimizations here">array</weak_warning>
        ('0' => '', '1' => null, '2' => null, '3' => '', '4' => null);

    /* false-positives */
    array();
    array(0 => $x,   1 => $y);
    array(0 => $x,  $i => $y);
    array($i => $x, $i => $y);