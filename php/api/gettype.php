<?php
    $x =0;

    $x = gettype($x) === 'unknown type';

    $x = gettype($x) === 'oops';    // <- reported error
    $x = gettype($x) === 'double';  // <- reported warning
    $x = gettype($x) === 'integer'; // <- reported warning

    $x = gettype($x) === 'string';  // <- reported warning
    $x = gettype($x) !== 'string';  // <- reported warning
    $x = 'string' === gettype($x);  // <- reported warning
    $x = 'string' !== gettype($x);  // <- reported warning

    $x = gettype($x) == 'string';   // <- reported warning
    $x = gettype($x) != 'string';   // <- reported warning
    $x = 'string' == gettype($x);   // <- reported warning
    $x = 'string' != gettype($x);   // <- reported warning