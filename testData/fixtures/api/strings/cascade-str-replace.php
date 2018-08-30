<?php

    $str1 = str_replace('-', '_', '...');

    /* sequential calls pattern */
    $str  = str_replace('-', '_', '...');
    /** dock-block should not break inspection */
    /** multiple dock-blocks should not break inspection */
    $str = <warning descr="This str_replace(...) call can be merged with the previous.">str_replace(' ', '-', $str)</warning>;
    /** dock-block should not break inspection */
    /** multiple dock-blocks should not break inspection */
    $str = <warning descr="This str_replace(...) call can be merged with the previous.">str_replace(' ', '-', $str)</warning>;

    /* nested calls pattern */
    $nested = str_replace(
        ' ', '-',
        <warning descr="This str_replace(...) call can be merged with its parent.">str_replace(' ', '-', '...')</warning>
    );

    /* parameters simplification pattern */
    $simplify = str_replace(
        array ('1', '1'),
        <weak_warning descr="Can be replaced with the string from the array.">array('1', '1')</weak_warning>,
        '...'
    );
    $simplify = str_replace(
        <weak_warning descr="Can be replaced with the string from the array.">array('1', '1')</weak_warning>,
        '1',
        '...'
    );

    function return_case_holder($x) {
        $x = str_replace('...', '', $x);
        return <warning descr="This str_replace(...) call can be merged with the previous.">str_replace('...', '', $x)</warning>;
    }

    /* false-positives: source and result storage containers are not matching */
    $a = str_replace('', '', '');
    $b = str_replace('', '', $a);

    /* QF correctness cases */
    function fix_correctness($x, $y, $z, $t)
    {
        $one = str_replace('1', '.', '...');
        $one = <warning descr="This str_replace(...) call can be merged with the previous.">str_replace('2', '.', $one)</warning>;
        $one = <warning descr="This str_replace(...) call can be merged with the previous.">str_replace('3', '_', $one)</warning>;

        $two = str_replace('3', '_', <warning descr="This str_replace(...) call can be merged with its parent.">str_replace('2', '.', str_replace('1', '.', '...'))</warning>);

        $three = str_replace('1', '.', <warning descr="This str_replace(...) call can be merged with its parent.">str_replace('2', '.', '...')</warning>);
        $four  = str_replace('1', '.', <warning descr="This str_replace(...) call can be merged with its parent.">str_replace(['2'], ['.'], '...')</warning>);
        $five  = str_replace(<weak_warning descr="Can be replaced with the string from the array.">['1']</weak_warning>, '.', <warning descr="This str_replace(...) call can be merged with its parent.">str_replace('2', '.', '...')</warning>);
        $six   = str_replace(<weak_warning descr="Can be replaced with the string from the array.">['1']</weak_warning>, '.', <warning descr="This str_replace(...) call can be merged with its parent.">str_replace(['2'], ['.'], '...')</warning>);

        $seven = str_replace(<weak_warning descr="Can be replaced with the string from the array.">['1']</weak_warning>, 'x', <warning descr="This str_replace(...) call can be merged with its parent.">str_replace(['2'], ['y'], '...')</warning>);
        $eight = str_replace(['1', '2'], 'x', <warning descr="This str_replace(...) call can be merged with its parent.">str_replace(['3'], 'y', '...')</warning>);

        $ten    = str_replace($x, $y, <warning descr="This str_replace(...) call can be merged with its parent.">str_replace('x', 'y', '...')</warning>);
        $eleven = str_replace('x', 'y', <warning descr="This str_replace(...) call can be merged with its parent.">str_replace($z, $t, '...')</warning>);

        $thirteen = str_replace([$one, $two], [$one, $two], '___');
        $thirteen = <warning descr="This str_replace(...) call can be merged with the previous.">str_replace($three, $three, $thirteen)</warning>;
    }