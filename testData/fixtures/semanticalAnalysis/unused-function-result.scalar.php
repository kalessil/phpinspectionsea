<?php

function cases_holder() {
    <warning descr="[EA] Function result is not used.">trim</warning>('');
    <warning descr="[EA] Function result is not used.">array_unique</warning>([]);

    /* false-positives: certain API functions */
    reset([]);

    /* types identification */
    ignored();
    <warning descr="[EA] Function result is not used.">reported_because_of_float</warning>();
    <warning descr="[EA] Function result is not used.">reported_because_of_mixed</warning>();

    /* fluent interfaces */
    (new Clazz())->returnThis();
    (new Clazz())->returnSelf();
    (new Clazz())->returnStatic();

    (new Clazz())->returnObject();
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

    /** @return $this */
    public function returnThis() {}
    /** @return self */
    public function returnSelf() {}
    /** @return static */
    public function returnStatic() {}

    /** @return object */
    public function returnObject() {}
}