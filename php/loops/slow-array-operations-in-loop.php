<?php

    $array = [];
    for ($i = 0; $i < 10; ++$i) {
        $array = array_merge($array, [$i]);   // <- reported
        $array = array_replace($array, [$i]); // <- reported
    }

    while (++$i < 10) {
        $array = array_merge($array, [$i]);   // <- reported
        $array = array_replace($array, [$i]); // <- reported
    }