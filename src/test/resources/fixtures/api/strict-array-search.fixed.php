<?php

    /* pattern: resolved types from expressions and not matching */
    array_search ('1', [], true);
    in_array ('1', [], true);

    /* pattern: resolved types not precise */
    /* @var int $needle */
    /* @var array $array */
    in_array($needle, $array, true);


    /* false-positives: 3rd parameter provided */
    array_search('1', [], true);
    in_array('1', [], false);

    /* false-positives: resolved types seems correct */
    /* @var int $needle */
    /* @var int[] $array */
    in_array($needle, $array);