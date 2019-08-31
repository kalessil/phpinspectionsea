<?php

function cases_holder(string $string, int $int)
{
    /* typos in logical operands */
    if ($string && $int <error descr="It was probably was intended to use && here (one of arguments is not an integer).">&</error> $string) {}
    if ($string || $int <error descr="It was probably was intended to use && here (one of arguments is not an integer).">&</error> $string) {}
    if ($string && $int <error descr="It was probably was intended to use || here (one of arguments is not an integer).">|</error> $string) {}
    if ($string || $int <error descr="It was probably was intended to use || here (one of arguments is not an integer).">|</error> $string) {}

    /* same, but with other tree structure */
    if ($int <error descr="It was probably was intended to use && here (one of arguments is not an integer).">&</error> $string && $string) {}
    if ($int <error descr="It was probably was intended to use && here (one of arguments is not an integer).">&</error> $string || $string) {}
    if ($int <error descr="It was probably was intended to use || here (one of arguments is not an integer).">|</error> $string && $string) {}
    if ($int <error descr="It was probably was intended to use || here (one of arguments is not an integer).">|</error> $string || $string) {}

    /* false-positives: valid cases */
    if ($int & ~$int) {}
    if ($int & $int & $int) {}
    if ($int | $int | $int) {}
    if ($string && $int & $int) {}
    if ($string || $int | $int) {}

    /* false-positives: an operand is a number */
    if ($string & 0) {}
    if ($string & 00) {}
    if ($string & 0x0) {}
    if ($string & 0.0) {}
    if ($string & 0b0) {}
}