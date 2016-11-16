<?php

    $arr = array();
    <weak_warning descr="'$arr[] = ...' should be used instead (2x faster)">array_push($arr, '')</weak_warning>;

    /* valid cases */
    array_push($arr, '', '', '');
    array_push($arr, ...$arr);
    $ind = array_push($arr, 'new value');
    if (2 === array_push($arr, 'new value')) {
        unset($arr);
    }
