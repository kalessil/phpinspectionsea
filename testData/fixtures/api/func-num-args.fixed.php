<?php

function cases_holder($object) {
    func_num_args();

    $object->count(func_get_args());
    count($object->func_get_args());
}