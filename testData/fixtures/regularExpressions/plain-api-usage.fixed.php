<?php

    /* case: str_replace */
    str_replace("whatever", $replacement, $string);
    str_ireplace("whatever", $replacement, $string);
    /* false-positives */
    preg_replace('/whatever/',   $replacement, $string, 1);
    preg_replace('/^whatever/',  $replacement, $string);
    preg_replace('/whatever.+/', $replacement, $string);
    preg_replace(['/whatever/'], $replacement, $string);
    preg_replace(['/whatever/i'], $replacement, $string);

    /* case: strpos */
    false !== strpos($string, "+");
    false !== strpos($string, "whatever");
    false !== stripos($string, "whatever");
    0 === strpos($string, "+");
    0 === strpos($string, "whatever");
    0 === stripos($string, "whatever");
    -1 !== strpos($string, "+", -strlen("+"));
    -1 !== strpos($string, "whatever", -strlen("whatever"));
    -1 !== stripos($string, "whatever", -strlen("whatever"));

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
    ltrim($string, 'a');
    ltrim($string, 'a');
    rtrim($string, 'a');
    rtrim($string, 'a');
    trim($string, 'a');
    trim($string, 'a');
    ltrim($string);
    rtrim($string);
    trim($string);
    /* false-positives */
    preg_replace('/^a+/m', '', $string);
    preg_replace('/^a+/u', '', $string);
    preg_replace('/^a+/', 'b', $string);
    preg_replace('/a+$/', 'b', $string);
    preg_replace('/^.+/', '', $string);
    preg_replace('/^.*/', '', $string);
    preg_replace('/.+$/', '', $string);
    preg_replace('/.*$/', '', $string);
    preg_replace('/^.+|a*$/', '', $string);
    preg_replace('/^a*|.+$/', '', $string);
    preg_replace('/^a*|b+$/', '', $string);

    /* case: explode */
    explode(",", '');
    explode("text", '');
    explode(",", '', 2);
    /* false-positives */
    preg_split('/.+/', '');
    preg_split('/\b/', '');