<?php

    $arr = array();
    <warning descr="'$arr[] = ...' should be used instead (2x faster).">array_push($arr, '')</warning>;

    /* valid cases */
    array_push($arr, '', '', '');
    array_push($arr, ...$arr);
    $ind = array_push($arr, 'new value');
    if (2 === array_push($arr, 'new value')) {
        unset($arr);
    }
