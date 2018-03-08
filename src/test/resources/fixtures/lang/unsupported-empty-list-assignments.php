<?php

/* case 1: regular array syntax */
foreach ($array as <error descr="Provokes a PHP Fatal error (Cannot use empty list).">list</error>(, )) {}
<error descr="Provokes a PHP Fatal error (Cannot use empty list).">list</error>(, ) = $array;

/* false-positives: variable is presented */
foreach ($array as list($variable, )) {}
list($variable, ) = $array;

/* case 2: short array syntax; older PS parser doesn't support newer syntax */
// foreach ($array as [, ]) {}
// [, ] = $array;

/* false-positives: variable is presented; older PS parser doesn't support newer syntax */
// foreach ($array as [$variable, ]) {}
// [$variable, ] = $array;