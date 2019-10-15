<?php

function cases_holder($object) {
    <warning descr="[EA] 'func_num_args()' can be used instead.">count(func_get_args())</warning>;
    <warning descr="[EA] 'func_num_args()' can be used instead.">count(\func_get_args())</warning>;

    $object->count(func_get_args());
    count($object->func_get_args());
}