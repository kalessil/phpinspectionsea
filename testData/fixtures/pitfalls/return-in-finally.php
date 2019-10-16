<?php

function finally_cases_holder($argument, $exception)
{
    try {
        if ($argument) { return $argument; }
    } finally {
        <error descr="[EA] Voids all returned values and thrown exceptions from the try-block (returned values and exceptions are lost).">return $argument;</error>
    }

    try {
        if ($argument) { throw $exception; }
    } finally {
        <error descr="[EA] Voids all returned values and thrown exceptions from the try-block (returned values and exceptions are lost).">return $exception;</error>
    }

    /* false-positives */
    try {
    } finally {
        return $argument;
    }
}

function yield_cases_holder($argument): Generator
{
    yield $argument;
    <error descr="[EA] It was probably intended to use 'yield' or 'yield from' here.">return $argument;</error>

    /* false-positives */
    return;
}

function yeld_scoping($argument): Generator
{
    $f = function () use ($argument) {
        yield $argument;
    };
    return $f();
}