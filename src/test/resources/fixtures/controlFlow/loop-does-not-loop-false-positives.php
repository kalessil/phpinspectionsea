<?php

    foreach ([] as $x) {
        foreach ([] as $y) {
            foreach ([] as $z) {
                continue 3; // <- continues the top-level loop
            }
        }
        break;
    }

    foreach ([] as $x) {
        foreach ([] as $y) {
            continue 2; // <- continues the top-level loop
        }
        break;
    }

    foreach ([] as $x) {
        if ($x) {
            if ($y) {
                continue; // <- continues the loop
            }
        }
        break;
    }

    /* @var \Generator $generator */
    foreach ($generator as $value) {
        /* assign into local variable or use $value further */
        break;
    }