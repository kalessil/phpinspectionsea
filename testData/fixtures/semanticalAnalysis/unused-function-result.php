<?php

function cases_holder() {
    <warning descr="Function result is not used.">trim</warning>('');
    <warning descr="Function result is not used.">array_unique</warning>([]);

    /* false-positives: certain API functions */
    reset([]);

    /* types identification */
    ignored();
    <warning descr="Function result is not used.">reported_because_of_float</warning>();
    <warning descr="Function result is not used.">reported_because_of_mixed</warning>();
}

/** @return bool|int|void */
function ignored() {}

/** @return bool|int|void|float */
function reported_because_of_float() {}

/** @return bool|int|void|mixed */
function reported_because_of_mixed() {}

class Clazz extends \stdClass
{
    public function __construct()
    {
        parent::__construct();
    }
}