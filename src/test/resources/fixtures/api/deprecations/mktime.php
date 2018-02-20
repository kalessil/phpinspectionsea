<?php

    <weak_warning descr="You should use time() function instead (current usage produces a runtime warning).">mktime()</weak_warning>;
    mktime(0, 0, 0, 0, 0, 0, <warning descr="Parameter 'is_dst' is deprecated and removed in PHP 7.">-1</warning>);
    <weak_warning descr="You should use time() function instead (current usage produces a runtime warning).">gmmktime()</weak_warning>;
    gmmktime(0, 0, 0, 0, 0, 0, <warning descr="Parameter 'is_dst' is deprecated and removed in PHP 7.">-1</warning>);

    /* false-positives */
    mktime(0);
    gmmktime(0);

