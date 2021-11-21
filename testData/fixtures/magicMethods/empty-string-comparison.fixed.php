<?php

function checkUseCases(string $string, float $float, int $int)
{
    if ('' !== $string)  {}
    if ('' === $string) {}
    if ('' !== $string)     {}
    if ('' === $string)    {}

    if ($string === null || '' !== $string) {}
    if ($string !== null && '' !== $string) {}

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