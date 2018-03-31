<?php

    /* pattern: in-efficient nesting */
    echo <weak_warning descr="'strtolower(trim($string))' should be used instead.">trim(strtolower($string))</weak_warning>;
    echo <weak_warning descr="'strtolower(ltrim($string))' should be used instead.">ltrim(strtolower($string))</weak_warning>;
    echo <weak_warning descr="'strtolower(rtrim($string))' should be used instead.">rtrim(strtolower($string))</weak_warning>;
    echo <weak_warning descr="'strtolower(substr($string, 1))' should be used instead.">substr(strtolower($string), 1)</weak_warning>;
    echo <weak_warning descr="'strtolower(mb_substr($string, 1))' should be used instead.">mb_substr(strtolower($string), 1)</weak_warning>;

    /* valid cases */
    echo $x->trim($x->strtolower($string));
    echo $x->trim(strtolower($string));
    echo trim($x->strtolower($string));
    echo strtolower(trim($string));

    /* pattern: senseless nesting; same function */
    echo strtolower(<weak_warning descr="'strtolower(...)' makes no sense here.">strtolower($string)</weak_warning>);
    echo ucfirst(<weak_warning descr="'ucfirst(...)' makes no sense here.">ucfirst($string)</weak_warning>);
    /* pattern: senseless nesting; different functions */
    echo strtolower(<weak_warning descr="'ucfirst(...)' makes no sense here.">ucfirst($string)</weak_warning>);
    echo strtoupper(<weak_warning descr="'lcfirst(...)' makes no sense here.">lcfirst($string)</weak_warning>);
    echo mb_convert_case(<weak_warning descr="'ucwords(...)' makes no sense here.">ucwords($string)</weak_warning>);
    echo mb_strtolower(<weak_warning descr="'ucfirst(...)' makes no sense here.">ucfirst($string)</weak_warning>);
    echo mb_strtoupper(<weak_warning descr="'lcfirst(...)' makes no sense here.">lcfirst($string)</weak_warning>);

    /* valid cases */
    echo ucfirst(strtolower($string));
    echo lcfirst(strtoupper($string));
    echo ucwords(mb_convert_case($string));
    echo ucfirst(mb_strtolower($string));
    echo lcfirst(mb_strtoupper($string));

    /* ucword-specific test case */
    echo mb_convert_case(ucwords($string, '_'));