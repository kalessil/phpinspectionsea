<?php

use mktime;

function cases_holder() {
    <warning descr="You should use time() function instead (current usage produces a runtime warning).">mktime()</warning>;
    mktime(0, 0, 0, 0, 0, 0, <warning descr="Parameter 'is_dst' is deprecated and removed in PHP 7.">-1</warning>);
    <warning descr="You should use time() function instead (current usage produces a runtime warning).">gmmktime()</warning>;
    gmmktime(0, 0, 0, 0, 0, 0, <warning descr="Parameter 'is_dst' is deprecated and removed in PHP 7.">-1</warning>);

    /* false-positives */
    mktime(0);
    gmmktime(0);
}

