<?php

namespace {
    function my_function() {}
}

namespace Unqualified\References {

    echo \uniqid();
    echo \PHP_INT_MAX;

    echo \call_user_func('\my_function', '');
    echo \call_user_func_array('\my_function', []);
    echo \array_filter([], '\my_function');
    echo \array_map('\my_function', []);
    echo \array_walk([], '\my_function');
    echo \array_reduce([], '\my_function');

    /* case with double quotes */
    echo \array_reduce([], "\\my_function");

    /* define calls tree structure differs, special case */
    \define('', '');
    \define('', '');

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

namespace WithFunctionImports {
    use function define;
    define('', '');
}