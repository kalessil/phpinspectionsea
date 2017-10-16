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

    $string = '...';
    call_user_func (array($string, 'sum'), 1, 2, 3);