<?php

    <warning descr="'$string[$position]' might be used instead (invalid index accesses might show up).">substr($string, $position, 1)</warning>;
    <warning descr="'$string[strlen($string) - 1]' might be used instead (invalid index accesses might show up).">substr($string, -1, 1)</warning>;
    <warning descr="'$string[strlen($string) - $offset]' might be used instead (invalid index accesses might show up).">substr($string, -$offset, 1)</warning>;

    /* false-positives: ms_substr */
    mb_substr($string, $position, 1);
    mb_substr($string, -1, 1);
    mb_substr($string, -$offset, 1);

    /* false-positives: offset differs from 1 */
    substr($string, $position, -1);
    substr($string, $position, 5);

    /* false-positives: source types */
    substr(call(), $position, 1);
    substr((string)$source, $position, 1);