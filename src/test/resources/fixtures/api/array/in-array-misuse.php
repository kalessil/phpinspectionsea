<?php

function cases_holder() {
    /* comparison equivalent cases */
    $x = <warning descr="''1' == '2'' should be used instead.">in_array('1', ['2'], false)</warning>;
    $x = <warning descr="''1' === '2'' should be used instead.">in_array('1', ['2'], true)</warning>;
    $x = <warning descr="''1' == '2'' should be used instead.">in_array('1', ['2'])</warning>;
    $x = <warning descr="''1' == '2'' should be used instead.">in_array('1', [0 => '2'], false)</warning>;
    $x = <warning descr="''1' === '2'' should be used instead.">in_array('1', [0 => '2'], true)</warning>;
    $x = <warning descr="''1' == '2'' should be used instead.">in_array('1', [0 => '2'])</warning>;
    $x = <warning descr="''1' == '2'' should be used instead.">in_array('1', ['0' => '2'])</warning>;

    /* ensure old array style recognized */
    $x = <warning descr="''1' == '2'' should be used instead.">in_array('1', array('2'), false)</warning>;

    /* ensure nested binary expression works properly */
    $x = true  && <warning descr="''1' == '2'' should be used instead.">in_array('1', array('2'), false)</warning>;
    $x = false || <warning descr="''1' == '2'' should be used instead.">in_array('1', array('2'), false)</warning>;

    $x = <warning descr="''1' != '2'' should be used instead.">!in_array ('1', array('2'), false)</warning>;
    $x = <warning descr="''1' !== '2'' should be used instead.">false === in_array ('1', array('2'), true)</warning>;
    $x = <warning descr="''1' != '2'' should be used instead.">false == in_array ('1', array('2'), false)</warning>;
    $x = <warning descr="''1' !== '2'' should be used instead.">in_array ('1', array('2'), true) === false</warning>;
    $x = <warning descr="''1' != '2'' should be used instead.">in_array ('1', array('2'), false) == false</warning>;
    $x = <warning descr="''1' !== '2'' should be used instead.">true !== in_array ('1', array('2'), true)</warning>;
    $x = <warning descr="''1' != '2'' should be used instead.">true != in_array ('1', array('2'), false)</warning>;
    $x = <warning descr="''1' !== '2'' should be used instead.">in_array ('1', array('2'), true) !== true</warning>;
    $x = <warning descr="''1' != '2'' should be used instead.">in_array ('1', array('2'), false) != true</warning>;

    $x = <warning descr="''1' === '2'' should be used instead.">true === in_array ('1', array('2'), true)</warning>;
    $x = <warning descr="''1' == '2'' should be used instead.">true == in_array ('1', array('2'), false)</warning>;
    $x = <warning descr="''1' === '2'' should be used instead.">in_array ('1', array('2'), true) === true</warning>;
    $x = <warning descr="''1' == '2'' should be used instead.">in_array ('1', array('2'), false) == true</warning>;
    $x = <warning descr="''1' === '2'' should be used instead.">false !== in_array ('1', array('2'), true)</warning>;
    $x = <warning descr="''1' == '2'' should be used instead.">false != in_array ('1', array('2'), false)</warning>;
    $x = <warning descr="''1' === '2'' should be used instead.">in_array ('1', array('2'), true) !== false</warning>;
    $x = <warning descr="''1' == '2'' should be used instead.">in_array ('1', array('2'), false) != false</warning>;

    /* array_key_exists equivalent cases */
    $y = !<warning descr="'array_key_exists('0', ['item'])' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array('0', array_keys(['item']), false)</warning>;
    $y =  <warning descr="'array_key_exists('0', ['item'])' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array('0', array_keys(['item']), false)</warning>;
    $y =  <warning descr="'array_key_exists('0', ['item'])' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array('0', array_keys(['item']), true)</warning>;
    $y =  <warning descr="'array_key_exists('0', ['item'])' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array('0', array_keys(['item']))</warning>;


    /* false-positives */
    $x = in_array('1', array(), true);
    $x = in_array('1', array());
}
