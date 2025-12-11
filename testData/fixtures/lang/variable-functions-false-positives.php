<?php

    /* false-positives: triggered only in PHP 5.4+ */
    call_user_func ($callable, 1, 2, 3);

    /* false-positives: callback analysis limitations */
    call_user_func ("$class::sum", 1, 2, 3);
    call_user_func (array($classes[$i], "sum"), 1, 2, 3);
    call_user_func (array($classes->i, "sum"), 1, 2, 3);

    /* false-positives: IDE limitations */
    call_user_func (callReturningCallable()); // should be callReturningCallable()(), but PS not supporting the syntax

    /* false-positives: relative addressing */
    call_user_func (array('ClassVFChild', 'parent::sum'), 1, 2, 3);
    call_user_func (array($object, 'parent::sum'), 1, 2, 3);

    /* false-positive: <string>->method(...) breaks at runtime */
    $string = '...';
    call_user_func (array($string, 'sum'), 1, 2, 3);

    /* false-positive: call_user_func doesn't support arguments by reference */
    call_user_func_array($callable, [ &$argument ]);
    /* false-positive: variadic syntax constraints */
    call_user_func_array($callable, [ ...$argument, '...' ]);

    /* false-positive: call_user_func_array to call_user_func migrated code is broken */
    call_user_func_array([$this, 'test'], []);