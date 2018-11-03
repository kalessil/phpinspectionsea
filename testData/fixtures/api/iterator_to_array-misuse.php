<?php

function cases_holder($source)
{
    <warning descr="Consider using '$source->current()' instead (consumes less cpu and memory resources).">iterator_to_array($source, false)[0]</warning>;
    foreach (<warning descr="Consider using '$source' instead (consumes less cpu and memory resources).">iterator_to_array($source)</warning> as $value) {}
    foreach (<warning descr="Consider using '$source' instead (consumes less cpu and memory resources).">iterator_to_array($source)</warning> as $key => $value) {}

    /* false-positives: multiple cases */
    iterator_to_array($source, false)[1];
}