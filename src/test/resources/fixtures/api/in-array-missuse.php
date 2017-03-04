<?php

    /* comparison equivalent cases */
    $x = <error descr="''2' == '1'' should be used instead.">in_array</error>  ('1', ['2'], false);
    $x = <error descr="''2' === '1'' should be used instead.">in_array</error> ('1', ['2'], true);
    $x = <error descr="''2' == '1'' should be used instead.">in_array</error>  ('1', ['2']);
    $x = <error descr="''2' == '1'' should be used instead.">in_array</error>  ('1', [0 => '2'], false);
    $x = <error descr="''2' === '1'' should be used instead.">in_array</error> ('1', [0 => '2'], true);
    $x = <error descr="''2' == '1'' should be used instead.">in_array</error>  ('1', [0 => '2']);
    $x = <error descr="''2' == '1'' should be used instead.">in_array</error>  ('1', ['0' => '2']);

    $x = <error descr="''2' == '1'' should be used instead.">in_array</error> ('1', array('2'), false);
    $x = true  && <error descr="''2' == '1'' should be used instead.">in_array</error> ('1', array('2'), false);
    $x = false || <error descr="''2' == '1'' should be used instead.">in_array</error> ('1', array('2'), false);



    /* array_key_exists equivalent cases */
    $y = !<error descr="'array_key_exists('0', ['item'])' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array('0', array_keys(['item']), false)</error>;
    $y =  <error descr="'array_key_exists('0', ['item'])' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array('0', array_keys(['item']), false)</error>;
    $y =  <error descr="'array_key_exists('0', ['item'])' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array('0', array_keys(['item']), true)</error>;
    $y =  <error descr="'array_key_exists('0', ['item'])' should be used instead. It is safe to refactor for type-safe code when the indexes are integers/strings only.">in_array('0', array_keys(['item']))</error>;


    /* false-positives */
    $x = in_array('1', array(), true);
    $x = in_array('1', array());
