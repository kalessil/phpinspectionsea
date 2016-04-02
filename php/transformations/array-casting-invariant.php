<?php

    $array = [];
    if (!is_array($array)) {                        // <- reported
        $array = [$array];
    }

    $array = is_array($array)  ? $array : [$array]; // <- reported
    $array = !is_array($array) ? [$array] : $array; // <- must be reported, but not!