<?php

[$a, $b] = [0, 1];

$source = [[0, 1]];
foreach ($source as [$c, $d]) {
    echo $c, $d;
}