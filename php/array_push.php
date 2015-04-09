<?php

    $arr = array();

    // warn here
    array_push($arr, '');

    // no issues here
    array_push($arr, '', '', '');
    $ind = array_push($arr, 'new value');
    if (2 === array_push($arr, 'new value')) {
        unset($arr);
    }
