<?php

    /* pattern: in-efficient nesting */
    echo <weak_warning descr="'strtolower(trim($string))' should be used instead.">trim</weak_warning>(strtolower($string));
    echo <weak_warning descr="'strtolower(ltrim($string))' should be used instead.">ltrim</weak_warning>(strtolower($string));
    echo <weak_warning descr="'strtolower(rtrim($string))' should be used instead.">rtrim</weak_warning>(strtolower($string));
    echo <weak_warning descr="'strtolower(substr($string, 1))' should be used instead.">substr</weak_warning>(strtolower($string), 1);
    echo <weak_warning descr="'strtolower(mb_substr($string, 1))' should be used instead.">mb_substr</weak_warning>(strtolower($string), 1);

    /* valid cases */
    echo strtolower(trim($string));

    /* pattern: senseless nesting; same function */
    echo strtolower(<weak_warning descr="'strtolower(...)' makes no sense here.">strtolower</weak_warning>($string));
    echo ucfirst(<weak_warning descr="'ucfirst(...)' makes no sense here.">ucfirst</weak_warning>($string));
    /* pattern: senseless nesting; different functions */
    echo strtolower(<weak_warning descr="'ucfirst(...)' makes no sense here.">ucfirst</weak_warning>($string));
    echo strtoupper(<weak_warning descr="'lcfirst(...)' makes no sense here.">lcfirst</weak_warning>($string));
    echo mb_convert_case(<weak_warning descr="'ucwords(...)' makes no sense here.">ucwords</weak_warning>($string));
    echo mb_strtolower(<weak_warning descr="'ucfirst(...)' makes no sense here.">ucfirst</weak_warning>($string));
    echo mb_strtoupper(<weak_warning descr="'lcfirst(...)' makes no sense here.">lcfirst</weak_warning>($string));

    /* valid cases */
    echo ucfirst(strtolower($string));
    echo lcfirst(strtoupper($string));
    echo ucwords(mb_convert_case($string));
    echo ucfirst(mb_strtolower($string));
    echo lcfirst(mb_strtoupper($string));