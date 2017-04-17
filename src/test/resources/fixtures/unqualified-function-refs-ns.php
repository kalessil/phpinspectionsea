<?php

namespace {
    function my_function() {}
}

namespace Unqualified\References {

    echo <weak_warning descr="Using '\uniqid(...)' would enable some of opcache optimizations">uniqid()</weak_warning>;
    echo <weak_warning descr="Using '\PHP_INT_MAX' would enable some of opcache optimizations">PHP_INT_MAX</weak_warning>;

    echo \call_user_func(<weak_warning descr="Using '\my_function' would enable some of opcache optimizations">'my_function'</weak_warning>, '');
    echo \call_user_func_array(<weak_warning descr="Using '\my_function' would enable some of opcache optimizations">'my_function'</weak_warning>, []);
    echo \array_filter([], <weak_warning descr="Using '\my_function' would enable some of opcache optimizations">'my_function'</weak_warning>);
    echo \array_map(<weak_warning descr="Using '\my_function' would enable some of opcache optimizations">'my_function'</weak_warning>, []);
    echo \array_walk([], <weak_warning descr="Using '\my_function' would enable some of opcache optimizations">'my_function'</weak_warning>);
    echo \array_reduce([], <weak_warning descr="Using '\my_function' would enable some of opcache optimizations">'my_function'</weak_warning>);

    echo \array_reduce([], <weak_warning descr="Using '\my_function' would enable some of opcache optimizations">"my_function"</weak_warning>);

    /* false-positives: a qualified call */
    echo \uniqid();
    echo \PHP_INT_MAX;

    /* false-positives: a in-ns function */
    function compact(...$variables) { return []; }
    echo \implode(',', compact('one', 'two'));

    /* false-positives: a in-ns constant */
    const PHP_INT_ZERO = 0;
    echo PHP_INT_ZERO;
}