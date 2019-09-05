<?php

    $x = <weak_warning descr="'$x > 0' would make more sense here (reduces cyclomatic and cognitive complexity).">$x > 0 ? true : false</weak_warning>;
    $x = <weak_warning descr="'$x <= 0' would make more sense here (reduces cyclomatic and cognitive complexity).">$x > 0 ? false : true</weak_warning>;
    $x = <weak_warning descr="'(bool)($x & 0)' would make more sense here (reduces cyclomatic and cognitive complexity).">$x & 0 ? true : false</weak_warning>;
    $x = <weak_warning descr="'!($x & 0)' would make more sense here (reduces cyclomatic and cognitive complexity).">$x & 0 ? false : true</weak_warning>;
    $x = <weak_warning descr="'($x && $y)' would make more sense here (reduces cyclomatic and cognitive complexity).">$x && $y ? true : false</weak_warning>;
    $x = <weak_warning descr="'!($x && $y)' would make more sense here (reduces cyclomatic and cognitive complexity).">$x && $y ? false : true</weak_warning>;

    $x = <weak_warning descr="'$x > 0' would make more sense here (reduces cyclomatic and cognitive complexity).">$x > 0 ? (true) : false</weak_warning>;
    $x = <weak_warning descr="'$x > 0' would make more sense here (reduces cyclomatic and cognitive complexity).">$x > 0 ? true : (false)</weak_warning>;
    $x = <weak_warning descr="'!is_numeric($x)' would make more sense here (reduces cyclomatic and cognitive complexity).">is_numeric($x) ? false : true</weak_warning>;

    $x = $x > 0 ? true : null;
    $x = trim($x) ? false : true;

    $x = <weak_warning descr="'empty($x)' would make more sense here (reduces cyclomatic and cognitive complexity).">empty($x) ? true : false</weak_warning>;
    $x = <weak_warning descr="'!empty($x)' would make more sense here (reduces cyclomatic and cognitive complexity).">!empty($x) ? true : false</weak_warning>;
    $x = <weak_warning descr="'!empty($x)' would make more sense here (reduces cyclomatic and cognitive complexity).">empty($x) ? false : true</weak_warning>;
    $x = <weak_warning descr="'empty($x)' would make more sense here (reduces cyclomatic and cognitive complexity).">!empty($x) ? false : true</weak_warning>;

    $x = empty($x) ? true : null;

    $x = <weak_warning descr="'isset($x)' would make more sense here (reduces cyclomatic and cognitive complexity).">isset($x) ? true : false</weak_warning>;
    $x = <weak_warning descr="'!isset($x)' would make more sense here (reduces cyclomatic and cognitive complexity).">isset($x) ? false : true</weak_warning>;
    $x = <weak_warning descr="'isset($x)' would make more sense here (reduces cyclomatic and cognitive complexity).">!isset($x) ? false : true</weak_warning>;

    $x = isset($x) ? false : null;

    function returns_bool(): bool { return true; }
    $x = <weak_warning descr="'returns_bool()' would make more sense here (reduces cyclomatic and cognitive complexity).">returns_bool() ? true : false</weak_warning>;
    $x = <weak_warning descr="'!returns_bool()' would make more sense here (reduces cyclomatic and cognitive complexity).">!returns_bool() ? true : false</weak_warning>;
    $x = <weak_warning descr="'!returns_bool()' would make more sense here (reduces cyclomatic and cognitive complexity).">returns_bool() ? false : true</weak_warning>;
    $x = <weak_warning descr="'returns_bool()' would make more sense here (reduces cyclomatic and cognitive complexity).">!returns_bool() ? false : true</weak_warning>;
