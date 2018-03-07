<?php

/* case 1: regular array syntax */
foreach ($array as list(, )) {}
list(, ) = $array;

/* false-positives: variable is presented */
foreach ($array as list($variable, )) {}
list($variable, ) = $array;

/* case 2: short array syntax */
foreach ($array as [, ]) {}
[, ] = $array;

/* false-positives: variable is presented */
foreach ($array as [$variable, ]) {}
[$variable, ] = $array;