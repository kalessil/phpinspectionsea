<?php

    $x = <weak_warning descr="'$x > 0' should be used instead (reduces cyclomatic and cognitive complexity).">$x > 0 ? true : false</weak_warning>;
    $x = <weak_warning descr="'$x <= 0' should be used instead (reduces cyclomatic and cognitive complexity).">$x > 0 ? false : true</weak_warning>;
    $x = <weak_warning descr="'(bool)($x & 0)' should be used instead (reduces cyclomatic and cognitive complexity).">$x & 0 ? true : false</weak_warning>;
    $x = <weak_warning descr="'!($x & 0)' should be used instead (reduces cyclomatic and cognitive complexity).">$x & 0 ? false : true</weak_warning>;
    $x = <weak_warning descr="'($x && $y)' should be used instead (reduces cyclomatic and cognitive complexity).">$x && $y ? true : false</weak_warning>;
    $x = <weak_warning descr="'!($x && $y)' should be used instead (reduces cyclomatic and cognitive complexity).">$x && $y ? false : true</weak_warning>;

    $x = <weak_warning descr="'$x > 0' should be used instead (reduces cyclomatic and cognitive complexity).">$x > 0 ? (true) : false</weak_warning>;
    $x = <weak_warning descr="'$x > 0' should be used instead (reduces cyclomatic and cognitive complexity).">$x > 0 ? true : (false)</weak_warning>;

    $x = $x > 0 ? true : null;
    $x = is_numeric($x) ? false : true;