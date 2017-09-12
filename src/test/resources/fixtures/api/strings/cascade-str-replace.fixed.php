<?php

    $str1 = str_replace('-', '_', '...');

    /* sequential calls pattern */
/** dock-block should not break inspection */
    /** multiple dock-blocks should not break inspection */
/** dock-block should not break inspection */
    /** multiple dock-blocks should not break inspection */
    $str = str_replace(array('-', ' ', ' '), array('_', '-', '-'), '...');

    /* nested calls pattern */
    $nested = str_replace(
        array(' ', ' '), '-',
        '...'
    );

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
        return str_replace(array('...', '...'), array('', ''), $x);
    }

    /* false-positives: source and result storage containers are not matching */
    $a = str_replace('', '', '');
    $b = str_replace('', '', $a);

    /* QF correctness cases */
    function fix_correctness($x, $y, $z, $t)
    {
        $one = str_replace(array('1', '2', '3'), array('.', '.', '_'), '...');

        $two = str_replace(array('2', '3'), array('.', '_'), str_replace('1', '.', '...'));

        $three = str_replace(array('2', '1'), '.', '...');
        $four  = str_replace(array('2', '1'), '.', '...');
        $five  = str_replace(array('2', '1'), '.', '...');
        $six   = str_replace(array('2', '1'), '.', '...');

        $seven = str_replace(array('2', '1'), array('y', 'x'), '...');
        $eight = str_replace(['3', '1', '2'], array('y', 'x', 'x'), '...');

        $ten    = str_replace(array('x', $x), array('y', $y), '...');
        $eleven = str_replace(array($z, 'x'), array($t, 'y'), '...');
    }