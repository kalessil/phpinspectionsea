<?php

    /* case: str_replace */
    <warning descr="'str_replace(\"whatever\", $replacement, $string)' can be used instead.">preg_replace('/whatever/', $replacement, $string)</warning>;
    <warning descr="'str_ireplace(\"whatever\", $replacement, $string)' can be used instead.">preg_replace('/whatever/i', $replacement, $string)</warning>;
    /* false-positives */
    preg_replace('/whatever/',   $replacement, $string, 1);
    preg_replace('/^whatever/',  $replacement, $string);
    preg_replace('/whatever.+/', $replacement, $string);

    /* case: strpos */
    <warning descr="'false !== strpos($string, \"whatever\")' can be used instead.">preg_match('/whatever/', $string)</warning>;
    <warning descr="'false !== stripos($string, \"whatever\")' can be used instead.">preg_match('/whatever/i', $string)</warning>;
    <warning descr="'0 === strpos($string, \"whatever\")' can be used instead.">preg_match('/^whatever/', $string)</warning>;
    <warning descr="'0 === stripos($string, \"whatever\")' can be used instead.">preg_match('/^whatever/i', $string)</warning>;
    /* false-positives */
    preg_match('/whatever.+/',  $string);
    preg_match('/whatever/',    $string, $matches);
    preg_match('/whatever/i',   $string, $matches);
    preg_match('/^whatever.+/', $string);
    preg_match('/^whatever/',   $string, $matches);
    preg_match('/^whatever/i',  $string, $matches);

    /* case: string comparison */
    <warning descr="'\"whatever\" === $string' can be used instead.">preg_match('/^whatever$/', $string)</warning>;
    /* false-positives */
    preg_match('/^whatever.+$/', $string);
    preg_match('/^whatever$/i',  $string);