<?php

    /* pattern: resolved types from expressions and not matching */
    <weak_warning descr="Third parameter should be provided to clarify if type safety is important in this context.">array_search ('1', [])</weak_warning>;
    <weak_warning descr="Third parameter should be provided to clarify if type safety is important in this context.">in_array ('1', [])</weak_warning>;

    /* pattern: resolved types not precise */
    /* @var int $needle */
    /* @var array $array */
    <weak_warning descr="Third parameter should be provided to clarify if type safety is important in this context.">in_array($needle, $array)</weak_warning>;


    /* false-positives: 3rd parameter provided */
    array_search('1', [], true);
    in_array('1', [], false);

    /* false-positives: resolved types seems correct */
    /* @var int $needle */
    /* @var int[] $array */
    in_array($needle, $array);