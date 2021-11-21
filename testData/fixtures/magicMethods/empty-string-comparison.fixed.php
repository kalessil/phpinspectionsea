<?php

function cases_holder(string $string, float $float, int $int)
{
    if ('' !== $string)  {}
    if ('' === $string) {}
    if ('' !== $string)     {}
    if ('' === $string)    {}

    if ('' === $string)  {}
    if ('' !== $string)  {}
    if ('' === $string) {}
    if ('' !== $string) {}

    if ('' === $string) {}
    if ('' !== $string) {}
    if ('' === $string) {}
    if ('' !== $string) {}

    if ('' === $string) {}
    if ('' !== $string)  {}
    if ('' !== $string) {}

    if ('' != $float) {}
    if ('' != $int) {}

    /* not yet supported */
    if (1 >  strlen($string)) {}
    if (1 <= strlen($string)) {}
}

function isolated_cases_holder()
{
    $string = '';

    if (false || '' !== $string) {}
    if (true && '' !== $string) {}
}