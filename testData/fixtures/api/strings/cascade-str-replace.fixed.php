<?php

    $str1 = str_replace('-', '_', '...');

    /* sequential calls pattern */
/** dock-block should not break inspection */
    /** multiple dock-blocks should not break inspection */
/** dock-block should not break inspection */
    /** multiple dock-blocks should not break inspection */
    $str = str_replace(['-', ' ', ' '], ['_', '-', '-'], '...');

    /* nested calls pattern */
    $nested = str_replace([' ', ' '], '-', '...');

    /* parameters simplification pattern */
    $simplify = str_replace(
        array ('1', '1'),
        '1',
        '...'
    );
    $simplify = str_replace(
        '1',
        '1',
        '...'
    );

    function return_case_holder($x) {
        return str_replace(['...', '...'], '', $x);
    }

    /* false-positives: source and result storage containers are not matching */
    $a = str_replace('', '', '');
    $b = str_replace('', '', $a);

    /* QF correctness cases */
    function fix_correctness($x, $y, $z, $t)
    {
        $one = str_replace(['1', '2', '3'], ['.', '.', '_'], '...');

        $two = str_replace(['2', '3'], ['.', '_'], str_replace('1', '.', '...'));

        $three = str_replace(['2', '1'], '.', '...');
        $four  = str_replace(['2', '1'], '.', '...');
        $five  = str_replace(['2', '1'], '.', '...');
        $six   = str_replace(['2', '1'], '.', '...');

        $seven = str_replace(['2', '1'], ['y', 'x'], '...');
        $eight = str_replace(['3', '1', '2'], ['y', 'x', 'x'], '...');

        $ten    = str_replace(['x', $x], ['y', $y], '...');
        $eleven = str_replace([$z, 'x'], [$t, 'y'], '...');

        $thirteen = str_replace([$one, $two, $three], [$one, $two, $three], '___');
    }

    function strireplace_cases() {
        $one = str_ireplace(['...', '...'], '...', '');

        $two = str_ireplace(['...', '...'], '...', '');

        /* false-positives: mixed calls */
        $three = str_ireplace('...', '...', str_replace('...', '...', ''));
        $four = str_replace('...', '...', str_ireplace('...', '...', ''));

        $five = str_ireplace('...', '...', '');
        $five = str_replace('...', '...', $five);

        $six = str_ireplace('...', '...', '');
        $six = str_replace('...', '...', $six);
    }