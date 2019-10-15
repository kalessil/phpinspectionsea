<?php

function cases_holder(string $string, int $position, int $offset)
{
    <warning descr="[EA] '$string[$position]' might be used instead (invalid index accesses might show up).">substr($string, $position, 1)</warning>;
    <warning descr="[EA] '$string[strlen($string) - 1]' might be used instead (invalid index accesses might show up).">substr($string, -1, 1)</warning>;
    <warning descr="[EA] '$string[strlen($string) - $offset]' might be used instead (invalid index accesses might show up).">substr($string, -$offset, 1)</warning>;

    /* false-positives: ms_substr */
    mb_substr($string, $position, 1);
    mb_substr($string, -1, 1);
    mb_substr($string, -$offset, 1);

    /* false-positives: offset differs from 1 */
    substr($string, $position, -1);
    substr($string, $position, 5);

    /* false-positives: source expression */
    substr(call(), $position, 1);
    substr((string)$string, $position, 1);

    /* false-positives: source type */
    substr($position, $position, 1);
}