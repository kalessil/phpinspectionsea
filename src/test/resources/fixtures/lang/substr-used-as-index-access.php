<?php

    <warning descr="'$string[$position]' might be used instead (invalid index accesses might show up)">substr($string, $position, 1)</warning>;
    <warning descr="'$string[$position]' might be used instead (invalid index accesses might show up)">mb_substr($string, $position, 1)</warning>;

    <warning descr="'$string[mb_strlen($string) -1]' might be used instead (invalid index accesses might show up)">mb_substr($string, -1, 1)</warning>;
    <warning descr="'$string[strlen($string) -1]' might be used instead (invalid index accesses might show up)">substr($string, -1, 1)</warning>;

    <warning descr="'$string[mb_strlen($string) -$offset]' might be used instead (invalid index accesses might show up)">mb_substr($string, -$offset, 1)</warning>;
    <warning descr="'$string[strlen($string) -$offset]' might be used instead (invalid index accesses might show up)">substr($string, -$offset, 1)</warning>;

    /* false-positives */
    substr($string, $position, -1);
    substr($string, $position, 5);
