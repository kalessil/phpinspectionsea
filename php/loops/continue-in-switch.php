<?php

foreach(array() as $item) {
    switch ($item) {
        case 0:
            continue 2;

        case 1:
            continue; // <- reported


        case 2:
            foreach (array() as $x) {
                continue;
            }
        break;

        default:
            break;
    }
}