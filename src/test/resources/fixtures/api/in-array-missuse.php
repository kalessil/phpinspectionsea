<?php

    /* comparison equivalent cases */
    $x = <error descr="''2' == '1'' should be used instead.">in_array</error> ('1', array('2'), false);
    $x = <error descr="''2' === '1'' should be used instead.">in_array</error> ('1', array('2'), true);
    $x = <error descr="''2' == '1'' should be used instead.">in_array</error> ('1', array('2'));
    $x = <error descr="''2' == '1'' should be used instead.">in_array</error> ('1', array(0 => '2'), false);
    $x = <error descr="''2' === '1'' should be used instead.">in_array</error> ('1', array(0 => '2'), true);
    $x = <error descr="''2' == '1'' should be used instead.">in_array</error> ('1', array(0 => '2'));
    $x = <error descr="''2' == '1'' should be used instead.">in_array</error> ('1', array('0' => '2'));

    /* array_key_exists equivalent cases */
    $y = <error descr="This looks equivalent to a array_key_exists(...) call. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array</error> ('0', array_keys(array('item')), false);
    $y = <error descr="This looks equivalent to a array_key_exists(...) call. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array</error> ('0', array_keys(array('item')), true);
    $y = <error descr="This looks equivalent to a array_key_exists(...) call. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array</error> ('0', array_keys(array('item')));


    /* false-positives */
    $x = in_array('1', array(), true);
    $x = in_array('1', array());
