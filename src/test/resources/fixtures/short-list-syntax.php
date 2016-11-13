<?php

list($a, $b) = [0, 1];

$source = [[0, 1]];
foreach ($source as list($a, $b)) {
    echo $a, $b;
}