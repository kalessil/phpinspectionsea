<?php

function patterns() {
    while (++$index < 100) {
        foreach ([] as $value) {
            <warning descr="The array initialization is missing, please place it at a proper place.">$array[]</warning> = $value;
            <warning descr="The array initialization is missing, please place it at a proper place.">$array[$index][]</warning> = $value;
        }
    }

    while (++$index < 100) {
        $array []= $index;
    }

    return function ($parameter) use ($array) {
        foreach ([] as $source) {
            foreach ($source as $value) {
                /* false-positive: container is parameter of a use-variable */
                $array[] = $value;
                $parameter[] = $value;

                /* false-positive: container is introduced by foreach-loop */
                $source[] = [];
            }
        }
    };
}
