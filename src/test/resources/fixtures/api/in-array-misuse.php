<?php

    /* comparison equivalent cases */
    $x = <error descr="''2' == '1'' should be used instead.">in_array('1', ['2'], false)</error>;
    $x = <error descr="''2' === '1'' should be used instead.">in_array('1', ['2'], true)</error>;
    $x = <error descr="''2' == '1'' should be used instead.">in_array('1', ['2'])</error>;
    $x = <error descr="''2' == '1'' should be used instead.">in_array('1', [0 => '2'], false)</error>;
    $x = <error descr="''2' === '1'' should be used instead.">in_array('1', [0 => '2'], true)</error>;
    $x = <error descr="''2' == '1'' should be used instead.">in_array('1', [0 => '2'])</error>;
    $x = <error descr="''2' == '1'' should be used instead.">in_array('1', ['0' => '2'])</error>;

    /* ensure old array style recognized */
    $x = <error descr="''2' == '1'' should be used instead.">in_array('1', array('2'), false)</error>;

    /* ensure nested binary expression works properly */
    $x = true  && <error descr="''2' == '1'' should be used instead.">in_array('1', array('2'), false)</error>;
    $x = false || <error descr="''2' == '1'' should be used instead.">in_array('1', array('2'), false)</error>;

    $x = <error descr="''2' != '1'' should be used instead.">!in_array ('1', array('2'), false)</error>;
    $x = <error descr="''2' !== '1'' should be used instead.">false === in_array ('1', array('2'), true)</error>;
    $x = <error descr="''2' != '1'' should be used instead.">false == in_array ('1', array('2'), false)</error>;
    $x = <error descr="''2' !== '1'' should be used instead.">in_array ('1', array('2'), true) === false</error>;
    $x = <error descr="''2' != '1'' should be used instead.">in_array ('1', array('2'), false) == false</error>;
    $x = <error descr="''2' !== '1'' should be used instead.">true !== in_array ('1', array('2'), true)</error>;
    $x = <error descr="''2' != '1'' should be used instead.">true != in_array ('1', array('2'), false)</error>;
    $x = <error descr="''2' !== '1'' should be used instead.">in_array ('1', array('2'), true) !== true</error>;
    $x = <error descr="''2' != '1'' should be used instead.">in_array ('1', array('2'), false) != true</error>;

    $x = <error descr="''2' === '1'' should be used instead.">true === in_array ('1', array('2'), true)</error>;
    $x = <error descr="''2' == '1'' should be used instead.">true == in_array ('1', array('2'), false)</error>;
    $x = <error descr="''2' === '1'' should be used instead.">in_array ('1', array('2'), true) === true</error>;
    $x = <error descr="''2' == '1'' should be used instead.">in_array ('1', array('2'), false) == true</error>;
    $x = <error descr="''2' === '1'' should be used instead.">false !== in_array ('1', array('2'), true)</error>;
    $x = <error descr="''2' == '1'' should be used instead.">false != in_array ('1', array('2'), false)</error>;
    $x = <error descr="''2' === '1'' should be used instead.">in_array ('1', array('2'), true) !== false</error>;
    $x = <error descr="''2' == '1'' should be used instead.">in_array ('1', array('2'), false) != false</error>;

    /* array_key_exists equivalent cases */
    $y = !<error descr="'array_key_exists('0', ['item'])' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array('0', array_keys(['item']), false)</error>;
    $y =  <error descr="'array_key_exists('0', ['item'])' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array('0', array_keys(['item']), false)</error>;
    $y =  <error descr="'array_key_exists('0', ['item'])' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array('0', array_keys(['item']), true)</error>;
    $y =  <error descr="'array_key_exists('0', ['item'])' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array('0', array_keys(['item']))</error>;


    /* false-positives */
    $x = in_array('1', array(), true);
    $x = in_array('1', array());