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