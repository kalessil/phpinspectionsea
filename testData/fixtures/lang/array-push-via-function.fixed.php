<?php

    $array = [];
    $array[] = '...';

    /* valid cases */
    array_push($array, '...', '...', '...');
    array_push($array, ...$array);
    $index = array_push($array, '...');
    if (array_push($array, '...') === 2) {
        unset($array);
    }
    
    $array[count($array)] = '...';

    $array[] = '...';
    $array[count($array) + 1] = '...';
    $variable = $array[count($array)];
