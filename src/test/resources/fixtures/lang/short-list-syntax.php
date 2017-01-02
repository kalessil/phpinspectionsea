<?php

<weak_warning descr="'[...] = ...' can be used here.">list</weak_warning> ($a, $b) = [0, 1];

$source = [[0, 1]];
foreach ($source as <weak_warning descr="'foreach (... as [...])' can be used here.">list</weak_warning> ($a, $b)) {
    echo $a, $b;
}