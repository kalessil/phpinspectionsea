<?php

function cases_holder($source)
{
    yield from $source;
    yield from $source;

    /* false-positives: not one-to-one identity */
    foreach ($source as $value) {
        yield trim($value);
    }
    foreach ($source as $key => $value) {
        yield $key => trim($value);
    }
    foreach ($source as $key => $value) {
        yield $value;
    }
}