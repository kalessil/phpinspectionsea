<?php

    /* case: str_replace */
    str_replace("whatever", $replacement, $string);
    str_ireplace("whatever", $replacement, $string);
    /* false-positives */
    preg_replace('/whatever/',   $replacement, $string, 1);
    preg_replace('/^whatever/',  $replacement, $string);
    preg_replace('/whatever.+/', $replacement, $string);

    /* case: strpos */
    false !== strpos($string, "whatever");
    false !== stripos($string, "whatever");
    0 === strpos($string, "whatever");
    0 === stripos($string, "whatever");
    /* false-positives */
    preg_match('/whatever.+/',  $string);
    preg_match('/whatever/',    $string, $matches);
    preg_match('/whatever/i',   $string, $matches);
    preg_match('/^whatever.+/', $string);
    preg_match('/^whatever/',   $string, $matches);
    preg_match('/^whatever/i',  $string, $matches);

    /* case: string comparison */
    "whatever" === $string;
    /* false-positives */
    preg_match('/^whatever.+$/', $string);
    preg_match('/^whatever$/i',  $string);

    /* case: trim */
    preg_replace('/^a+/', '', $string);
    preg_replace('/^a*/', '', $string);
    preg_replace('/a+$/', '', $string);
    preg_replace('/a*$/', '', $string);
    preg_replace('/^a+|a*$/', '', $string);
    preg_replace('/^a*|a+$/', '', $string);
    /* false-positives */
    preg_replace('/^a+/', 'b', $string);
    preg_replace('/a+$/', 'b', $string);
    preg_replace('/^.+/', '', $string);
    preg_replace('/^.*/', '', $string);
    preg_replace('/.+$/', '', $string);
    preg_replace('/.*$/', '', $string);
    preg_replace('/^.+|a*$/', '', $string);
    preg_replace('/^a*|.+$/', '', $string);
    preg_replace('/^a*|b+$/', '', $string);