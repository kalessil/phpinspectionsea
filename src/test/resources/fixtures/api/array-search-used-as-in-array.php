<?php

    if (!<warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</warning>) {}
    if (<warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</warning>) {}

    if (<warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</warning> || false) {}
    if (<warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</warning> or false) {}
    if (<warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</warning> OR false) {}

    if (<warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</warning> && true) {}
    if (<warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</warning> and true) {}
    if (<warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array())</warning> AND true) {}

    if (<warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array()) === false</warning>) {}
    if (<warning descr="'in_array(...)' should be used instead (clearer intention).">false === array_search('', array())</warning>) {}

    if (array_search('', array()) !== <error descr="This makes no sense, as array_search(...) never returns true.">true</error>) {}
    if (<error descr="This makes no sense, as array_search(...) never returns true.">true</error> !== array_search('', array())) {}

    if (<warning descr="'in_array(...)' should be used instead (clearer intention).">array_search('', array()) !== false</warning>) {}
    if (<warning descr="'in_array(...)' should be used instead (clearer intention).">false !== array_search('', array())</warning>) {}

    if (array_search('', array()) === <error descr="This makes no sense, as array_search(...) never returns true.">true</error>) {}
    if (<error descr="This makes no sense, as array_search(...) never returns true.">true</error> === array_search('', array())) {}

    /* false-positives */
    $x = array_search('', []) ?: '...';
    $x = $x ?: array_search('', []);
    $x = $x ?? array_search('', []);