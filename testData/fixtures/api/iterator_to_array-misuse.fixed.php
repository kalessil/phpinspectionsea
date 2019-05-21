<?php

abstract class Clazz implements \Iterator {}

function cases_holder(\Iterator $iterator, \IteratorAggregate $aggregate)
{
    $iterator->current();
    foreach ($iterator as $value) {}
    foreach ($iterator as $key => $value) {}

    (new Clazz())->current();
    ($iterator ?? $iterator)->current();

    /* false-positives: not the first element */
    iterator_to_array($iterator, false)[1];
    /* false-positives: missing method */
    iterator_to_array($aggregate, false)[0];
}