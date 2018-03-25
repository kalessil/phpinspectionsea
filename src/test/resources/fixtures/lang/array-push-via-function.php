<?php

    $array = [];
    <warning descr="'$array[] = '...'' here would be up to 2x faster.">array_push($array, '...')</warning>;

    /* valid cases */
    array_push($array, '...', '...', '...');
    array_push($array, ...$array);
    $index = array_push($array, '...');
    if (array_push($array, '...') === 2) {
        unset($array);
    }
    
    $array[<warning descr="It seems that the index can be omitted at all.">count</warning>($array)] = '...';

    $array[] = '...';
    $array[count($array) + 1] = '...';
    $variable = $array[count($array)];
