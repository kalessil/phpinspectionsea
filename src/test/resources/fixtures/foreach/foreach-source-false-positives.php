<?php

    /* false-positive: global context influenced by foreach-source-false-positives.setup.php */
    $files = ['...'];
    foreach ($files as $file) {}

    /* false-positive: overriding variable before foreach */
    /** @param null|string|string[] $values */
    function mixedParams($values) {
        $values = is_array($values) ? $values : explode('...', $values);
        foreach ($values as $string) {}
    }

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

    /* ensures not reporting stub-functions */
    foreach (array_rand([]) as $random) {}