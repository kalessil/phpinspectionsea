<?php

function cases_holder() {
    /* comparison equivalent cases */
    $x = '2' == '1';
    $x = '2' === '1';
    $x = '2' == '1';
    $x = '2' == '1';
    $x = '2' === '1';
    $x = '2' == '1';
    $x = '2' == '1';

    /* ensure old array style recognized */
    $x = '2' == '1';

    /* ensure nested binary expression works properly */
    $x = true  && '2' == '1';
    $x = false || '2' == '1';

    $x = '2' != '1';
    $x = '2' !== '1';
    $x = '2' != '1';
    $x = '2' !== '1';
    $x = '2' != '1';
    $x = '2' !== '1';
    $x = '2' != '1';
    $x = '2' !== '1';
    $x = '2' != '1';

    $x = '2' === '1';
    $x = '2' == '1';
    $x = '2' === '1';
    $x = '2' == '1';
    $x = '2' === '1';
    $x = '2' == '1';
    $x = '2' === '1';
    $x = '2' == '1';

    /* array_key_exists equivalent cases */
    $y = !array_key_exists('0', ['item']);
    $y = array_key_exists('0', ['item']);
    $y = array_key_exists('0', ['item']);
    $y = array_key_exists('0', ['item']);


    /* false-positives */
    $x = in_array('1', array(), true);
    $x = in_array('1', array());
}