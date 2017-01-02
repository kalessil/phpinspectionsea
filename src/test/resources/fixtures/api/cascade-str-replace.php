<?php

    $str1 = str_replace('-', '_', 'Some text here');

    /* sequential calls pattern */
    $str  = str_replace('-', '_', 'Some text here');
    /** dock-block should not break inspection */
    /** multiple dock-blocks should not break inspection */
    $str = <warning descr="This str_replace(...) call can be merged with the previous.">str_replace</warning> (' ', '-', $str);
    /** dock-block should not break inspection */
    /** multiple dock-blocks should not break inspection */
    $str = <warning descr="This str_replace(...) call can be merged with the previous.">str_replace</warning> (' ', '-', $str);

    /* nested calls pattern */
    $str = str_replace(
        ' ', '-',
        <warning descr="This str_replace(...) call can be merged with its parent.">str_replace(' ', '-', $str)</warning>
    );

    /* parameters simplification pattern */
    $str2 = str_replace(
        array('1', '1'),
        <weak_warning descr="Can be replaced with the string duplicated in array.">array('1', '1')</weak_warning>,
        $str1
    );