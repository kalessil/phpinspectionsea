<?php

function finally_cases_holder($argument, $exception)
{
    try {
        if ($argument) { return $argument; }
    } finally {
        <error descr="Voids all returned values and thrown exceptions from the try-block (returned values and exceptions are lost).">return $argument;</error>
    }

    try {
        if ($argument) { throw $exception; }
    } finally {
        <error descr="Voids all returned values and thrown exceptions from the try-block (returned values and exceptions are lost).">return $exception;</error>
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
    <error descr="It was probably intended to use yield here (currently the returned values is getting ignored).">return $argument;</error>

    /* false-positives */
    return;
}