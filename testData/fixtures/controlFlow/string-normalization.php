<?php

    /* pattern: in-efficient nesting */
    echo <weak_warning descr="[EA] 'strtolower(trim($string))' makes more sense here.">trim(strtolower($string))</weak_warning>;
    echo <weak_warning descr="[EA] 'strtolower(ltrim($string))' makes more sense here.">ltrim(strtolower($string))</weak_warning>;
    echo <weak_warning descr="[EA] 'strtolower(rtrim($string))' makes more sense here.">rtrim(strtolower($string))</weak_warning>;
    echo <weak_warning descr="[EA] 'strtolower(substr($string, 1))' makes more sense here.">substr(strtolower($string), 1)</weak_warning>;
    echo <weak_warning descr="[EA] 'strtolower(mb_substr($string, 1))' makes more sense here.">mb_substr(strtolower($string), 1)</weak_warning>;

    /* more trim-related cases */
    echo <weak_warning descr="[EA] 'strtolower(trim($string, '...'))' makes more sense here.">trim(strtolower($string), '...')</weak_warning>;
    echo trim(strtolower($string), 'a');
    echo trim(strtolower($string), "A");
    echo trim(strtolower($string), $unknown);

    /* valid cases */
    echo $x->trim($x->strtolower($string));
    echo $x->trim(strtolower($string));
    echo trim($x->strtolower($string));
    echo strtolower(trim($string));

    /* pattern: senseless nesting; same function */
    echo strtolower(<weak_warning descr="[EA] 'strtolower(...)' makes no sense here.">strtolower($string)</weak_warning>);
    echo ucfirst(<weak_warning descr="[EA] 'ucfirst(...)' makes no sense here.">ucfirst($string)</weak_warning>);
    /* pattern: senseless nesting; different functions */
    echo strtolower(<weak_warning descr="[EA] 'ucfirst(...)' makes no sense here.">ucfirst($string)</weak_warning>);
    echo strtoupper(<weak_warning descr="[EA] 'lcfirst(...)' makes no sense here.">lcfirst($string)</weak_warning>);
    echo mb_convert_case(<weak_warning descr="[EA] 'ucwords(...)' makes no sense here.">ucwords($string)</weak_warning>);
    echo mb_strtolower(<weak_warning descr="[EA] 'ucfirst(...)' makes no sense here.">ucfirst($string)</weak_warning>);
    echo mb_strtoupper(<weak_warning descr="[EA] 'lcfirst(...)' makes no sense here.">lcfirst($string)</weak_warning>);

    /* valid cases */
    echo ucfirst(strtolower($string));
    echo lcfirst(strtoupper($string));
    echo ucwords(mb_convert_case($string));
    echo ucfirst(mb_strtolower($string));
    echo lcfirst(mb_strtoupper($string));

    /* ucword-specific test case */
    echo mb_convert_case(ucwords($string, '_'));