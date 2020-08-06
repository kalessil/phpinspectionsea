<?php

    /* case: str_replace */
    <warning descr="[EA] 'str_replace(\"whatever\", $replacement, $string)' can be used instead.">preg_replace('/whatever/', $replacement, $string)</warning>;
    <warning descr="[EA] 'str_ireplace(\"whatever\", $replacement, $string)' can be used instead.">preg_replace('/whatever/i', $replacement, $string)</warning>;
    /* false-positives */
    preg_replace('/whatever/',   $replacement, $string, 1);
    preg_replace('/^whatever/',  $replacement, $string);
    preg_replace('/whatever.+/', $replacement, $string);
    preg_replace(['/whatever/'], $replacement, $string);
    preg_replace(['/whatever/i'], $replacement, $string);

    /* case: strpos */
    <warning descr="[EA] 'false !== strpos($string, \"+\")' can be used instead.">preg_match('/\+/', $string)</warning>;
    <warning descr="[EA] 'false !== strpos($string, \"whatever\")' can be used instead.">preg_match('/whatever/', $string)</warning>;
    <warning descr="[EA] 'false !== stripos($string, \"whatever\")' can be used instead.">preg_match('/whatever/i', $string)</warning>;
    <warning descr="[EA] '0 === strpos($string, \"+\")' can be used instead.">preg_match('/^\+/', $string)</warning>;
    <warning descr="[EA] '0 === strpos($string, \"whatever\")' can be used instead.">preg_match('/^whatever/', $string)</warning>;
    <warning descr="[EA] '0 === stripos($string, \"whatever\")' can be used instead.">preg_match('/^whatever/i', $string)</warning>;
    <warning descr="[EA] '-1 !== strpos($string, \"+\", -strlen(\"+\"))' can be used instead.">preg_match('/\+$/', $string)</warning>;
    <warning descr="[EA] '-1 !== strpos($string, \"whatever\", -strlen(\"whatever\"))' can be used instead.">preg_match('/whatever$/', $string)</warning>;
    <warning descr="[EA] '-1 !== stripos($string, \"whatever\", -strlen(\"whatever\"))' can be used instead.">preg_match('/whatever$/i', $string)</warning>;

    /* false-positives */
    preg_match('/whatever.+/',  $string);
    preg_match('/whatever/',    $string, $matches);
    preg_match('/whatever/i',   $string, $matches);
    preg_match('/^whatever.+/', $string);
    preg_match('/^whatever/',   $string, $matches);
    preg_match('/^whatever/i',  $string, $matches);

    /* case: string comparison */
    <warning descr="[EA] '\"whatever\" === $string' can be used instead.">preg_match('/^whatever$/', $string)</warning>;
    /* false-positives */
    preg_match('/^whatever.+$/', $string);
    preg_match('/^whatever$/i',  $string);

    /* case: trim */
    <warning descr="[EA] 'ltrim($string, 'a')' can be used instead.">preg_replace('/^a+/', '', $string)</warning>;
    <warning descr="[EA] 'ltrim($string, 'a')' can be used instead.">preg_replace('/^a*/', '', $string)</warning>;
    <warning descr="[EA] 'rtrim($string, 'a')' can be used instead.">preg_replace('/a+$/', '', $string)</warning>;
    <warning descr="[EA] 'rtrim($string, 'a')' can be used instead.">preg_replace('/a*$/', '', $string)</warning>;
    <warning descr="[EA] 'trim($string, 'a')' can be used instead.">preg_replace('/^a+|a*$/', '', $string)</warning>;
    <warning descr="[EA] 'trim($string, 'a')' can be used instead.">preg_replace('/^a*|a+$/', '', $string)</warning>;
    <warning descr="[EA] 'ltrim($string)' can be used instead.">preg_replace('/^\s*/', '', $string)</warning>;
    <warning descr="[EA] 'rtrim($string)' can be used instead.">preg_replace('/\s*$/', '', $string)</warning>;
    <warning descr="[EA] 'trim($string)' can be used instead.">preg_replace('/^\s*|\s*$/', '', $string)</warning>;
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
    <warning descr="[EA] 'explode(\",\", '')' can be used instead.">preg_split('/,/', '')</warning>;
    <warning descr="[EA] 'explode(\"text\", '')' can be used instead.">preg_split('/text/', '')</warning>;
    <warning descr="[EA] 'explode(\",\", '', 2)' can be used instead.">preg_split('/,/', '', 2)</warning>;
    /* false-positives */
    preg_split('/.+/', '');
    preg_split('/\b/', '');