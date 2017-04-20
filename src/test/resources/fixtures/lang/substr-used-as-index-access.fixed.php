<?php

    $string[$position];
    $string[strlen($string) - 1];
    $string[strlen($string) - $offset];

    /* false-positives: ms_substr */
    mb_substr($string, $position, 1);
    mb_substr($string, -1, 1);
    mb_substr($string, -$offset, 1);

    /* false-positives: offset differs from 1 */
    substr($string, $position, -1);
    substr($string, $position, 5);
