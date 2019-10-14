<?php

use function mktime;

function cases_holder() {
    <warning descr="[EA] You should use time() function instead (current usage produces a runtime warning).">mktime()</warning>;
    mktime(0, 0, 0, 0, 0, 0, <warning descr="[EA] Parameter 'is_dst' is deprecated and removed in PHP 7.">-1</warning>);
    <warning descr="[EA] You should use time() function instead (current usage produces a runtime warning).">gmmktime()</warning>;
    gmmktime(0, 0, 0, 0, 0, 0, <warning descr="[EA] Parameter 'is_dst' is deprecated and removed in PHP 7.">-1</warning>);

    /* false-positives */
    mktime(0);
    gmmktime(0);
}

