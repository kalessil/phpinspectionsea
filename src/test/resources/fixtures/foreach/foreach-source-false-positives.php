<?php

    /* ensures that type[] is handled properly */
    /* @var $intOrIntArray int[] */
    foreach ($intOrIntArray as $value) {
        is_int($value);
    }

    /* general pattern: null as indication of failed operation */
    /* @var $nullOrArray null|array */
    foreach ($nullOrArray as $value) {
        is_int($value);
    }

    /* general pattern: false as indication of failed operation */
    /* @var $boolOrArray bool|array */
    foreach ($boolOrArray as $value) {
        is_int($value);
    }

    /* general pattern: generators and transitively iterators */
    /* @var $generator \Generator */
    foreach ($generator as $value) {
        is_int($value);
    }