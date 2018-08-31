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