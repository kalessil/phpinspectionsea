<?php

function cases_holder($source)
{
    $source->current();
    foreach ($source as $value) {}
    foreach ($source as $key => $value) {}

    /* false-positives: multiple cases */
    iterator_to_array($source, false)[1];
}