<?php

    $x = <weak_warning descr="'$x > 0' should be used instead.">$x > 0 ? true : false</weak_warning>;
    $x = <weak_warning descr="'$x <= 0' should be used instead.">$x > 0 ? false : true</weak_warning>;
    $x = <weak_warning descr="'(bool)($x & 0)' should be used instead.">$x & 0 ? true : false</weak_warning>;
    $x = <weak_warning descr="'!($x & 0)' should be used instead.">$x & 0 ? false : true</weak_warning>;
    $x = <weak_warning descr="'($x && $y)' should be used instead.">$x && $y ? true : false</weak_warning>;
    $x = <weak_warning descr="'!($x && $y)' should be used instead.">$x && $y ? false : true</weak_warning>;

    $x = <weak_warning descr="'$x > 0' should be used instead.">$x > 0 ? (true) : false</weak_warning>;
    $x = <weak_warning descr="'$x > 0' should be used instead.">$x > 0 ? true : (false)</weak_warning>;

    $x = $x > 0 ? true : null;
    $x = is_numeric($x) ? false : true;

    $x = <weak_warning descr="'call($x ? $x : null)' should be used instead.">$x ? call($x) : call(null)</weak_warning>;
    $x = <weak_warning descr="'call($x ? $x : null, null)' should be used instead.">$x ? call($x, null) : call(null, null)</weak_warning>;

    $x = $x ? call($x, $y) : call(null, null);
    $x = $x ? call1($x, null) : call2(null, null);