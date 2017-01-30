<?php

namespace UF\Refs;

    echo <weak_warning descr="Using '\uniqid(...)' would enable some of opcache optimizations">uniqid()</weak_warning>;
    echo <weak_warning descr="Using '\PHP_INT_MAX' would enable some of opcache optimizations">PHP_INT_MAX</weak_warning>;

    /* false-positives: a qualified call */
    echo \uniqid();
    echo \PHP_INT_MAX;

    /* false-positives: a in-ns function */
    function compact(...$variables) { return []; }
    echo \implode(',', compact('one', 'two'));

    /* false-positives: a in-ns constant */
    const PHP_INT_ZERO = 0;
    echo PHP_INT_ZERO;
