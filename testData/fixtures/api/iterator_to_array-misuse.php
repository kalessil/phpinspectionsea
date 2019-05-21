<?php

abstract class Clazz implements \Iterator {}

function cases_holder(\Iterator $iterator, \IteratorAggregate $aggregate)
{
    <warning descr="Consider using '$iterator->current()' instead (consumes less cpu and memory resources).">iterator_to_array($iterator, false)[0]</warning>;
    foreach (<warning descr="Consider using '$iterator' instead (consumes less cpu and memory resources).">iterator_to_array($iterator)</warning> as $value) {}
    foreach (<warning descr="Consider using '$iterator' instead (consumes less cpu and memory resources).">iterator_to_array($iterator)</warning> as $key => $value) {}

    <warning descr="Consider using '(new Clazz())->current()' instead (consumes less cpu and memory resources).">iterator_to_array(<error descr="Cannot instantiate abstract class 'Clazz'">new Clazz()</error>, false)[0]</warning>;
    <warning descr="Consider using '($iterator ?? $iterator)->current()' instead (consumes less cpu and memory resources).">iterator_to_array($iterator ?? $iterator, false)[0]</warning>;

    /* false-positives: not the first element */
    iterator_to_array($iterator, false)[1];
    /* false-positives: missing method */
    iterator_to_array($aggregate, false)[0];
}