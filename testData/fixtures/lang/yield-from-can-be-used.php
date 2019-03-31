<?php

function cases_holder($source)
{
    <warning descr="'yield from ...' can be used instead (generator delegation).">foreach</warning> ($source as $value) {
        yield $value;
    }
    <warning descr="'yield from ...' can be used instead (generator delegation).">foreach</warning> ($source as $key => $value) {
        yield $key => $value;
    }

    /* false-positives: not one-to-one identity */
    foreach ($source as $value) {
        yield trim($value);
    }
    foreach ($source as $value) {
        yield from $value;
    }
    foreach ($source as $key => $value) {
        yield $key => trim($value);
    }
    foreach ($source as $key => $value) {
        yield $value;
    }
}