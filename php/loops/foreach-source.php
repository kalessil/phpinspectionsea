<?php

    /* @var $mixed mixed */
    foreach ($mixed as $value) {    // <- reported (weak)
        is_int($value);
    }

    /* @var $unknown */
    foreach ($unknown as $value) {  // <- reported (weak)
        is_int($value);
    }
    foreach ($unknown->x as $value) {  // <- reported (weak)
        is_int($value);
    }
    foreach ($unknown[0] as $value) {  // <- reported (weak)
        is_int($value);
    }

    /* @var $object object */
    foreach ($mixed as $value) {    // <- reported (weak)
        is_int($value);
    }

    /* @var $int int */
    foreach ($int as $value) {      // <- reported (error)
        is_int($value);
    }

    /* @var $intOrIntArray int[] */
    foreach ($intOrIntArray as $value) {
        is_int($value);
    }

    /* @var $stdClass stdClass */
    foreach ($stdClass as $value) { // <- reported (error)
        is_int($value);
    }