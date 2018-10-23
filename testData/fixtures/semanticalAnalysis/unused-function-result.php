<?php

function cases_holder() {
    <warning descr="Function result is not used.">trim</warning>('');
    <warning descr="Function result is not used.">array_unique</warning>([]);

    /* false-positives: certain API functions */
    reset([]);

    /* types identification */
    ignored();
    <warning descr="Function result is not used.">reported</warning>();
}

/** @return bool|int|void */
function ignored() {}

/** @return bool|int|void|float */
function reported() {}

class Clazz extends \stdClass
{
    public function __construct()
    {
        parent::__construct();
    }
}