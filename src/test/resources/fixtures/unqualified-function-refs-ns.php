<?php

namespace UF\Refs;

    echo <weak_warning descr="Using '\uniqid(...)' would enable some of opcache optimizations">uniqid()</weak_warning>;

    /* false-positives: a qualified call */
    echo \uniqid();

    /* false-positives: a in-ns function */
    function compact(...$variables) { return []; }
    echo \implode(',', compact('one', 'two'));
