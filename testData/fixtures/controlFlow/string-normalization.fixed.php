<?php

    /* pattern: in-efficient nesting */
    echo strtolower(trim($string));
    echo strtolower(ltrim($string));
    echo strtolower(rtrim($string));
    echo strtolower(substr($string, 1));
    echo strtolower(mb_substr($string, 1));

    /* more trim-related cases */
    echo strtolower(trim($string, '...'));
    echo trim(strtolower($string), 'a');
    echo trim(strtolower($string), "A");
    echo trim(strtolower($string), $unknown);

    /* valid cases */
    echo $x->trim($x->strtolower($string));
    echo $x->trim(strtolower($string));
    echo trim($x->strtolower($string));
    echo strtolower(trim($string));

    /* pattern: senseless nesting; same function */
    echo strtolower($string);
    echo ucfirst($string);
    /* pattern: senseless nesting; different functions */
    echo strtolower($string);
    echo strtoupper($string);
    echo mb_convert_case($string);
    echo mb_strtolower($string);
    echo mb_strtoupper($string);

    /* valid cases */
    echo ucfirst(strtolower($string));
    echo lcfirst(strtoupper($string));
    echo ucwords(mb_convert_case($string));
    echo ucfirst(mb_strtolower($string));
    echo lcfirst(mb_strtoupper($string));

    /* ucword-specific test case */
    echo mb_convert_case(ucwords($string, '_'));