<?php

function cases_holder(string $string, ?string $nullableString, float $float, int $int)
{
    if ('' !== $string)  {}
    if ('' === $string) {}
    if ('' !== $string)     {}
    if ('' === $string)    {}

    if ('' !== (string)$nullableString)  {}
    if ('' === (string)$nullableString) {}
    if ('' !== (string)$nullableString)     {}
    if ('' === (string)$nullableString)    {}

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

    if ('' !== (string)$float) {}
    if ('' !== (string)$int) {}

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