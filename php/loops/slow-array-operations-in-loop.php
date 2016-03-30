<?php

    $array = [];
    for ($i = 0; $i < 10; ++$i) {
        $array = array_merge($array, [$i]); // <- reported
    }