<?php

function cases_holder($array) {
    array_map(<weak_warning descr="[EA] The closure can be replaced with 'trim' (reduces cognitive load).">function ($value) { return trim($value); }</weak_warning>, $array);
    array_filter($array, <weak_warning descr="[EA] The closure can be replaced with 'is_numeric' (reduces cognitive load).">function ($value) { return is_numeric($value); }</weak_warning>);
    array_walk($array, <weak_warning descr="[EA] The closure can be replaced with 'trim' (reduces cognitive load).">function($value) { return trim($value); }</weak_warning>);
    array_walk_recursive($array, <weak_warning descr="[EA] The closure can be replaced with 'trim' (reduces cognitive load).">function($value) { return trim($value); }</weak_warning>);
    array_walk_recursive($array, <weak_warning descr="[EA] The closure can be replaced with 'trim' (reduces cognitive load).">function($value) { $value = trim($value); }</weak_warning>);
    array_walk_recursive($array, <weak_warning descr="[EA] The closure can be replaced with 'is_null' (reduces cognitive load).">function($value) { return $value === null; }</weak_warning>);

    array_map(<weak_warning descr="[EA] The closure can be replaced with '\trim' (reduces cognitive load).">function ($value) { return \trim($value); }</weak_warning>, $array);

    /* false-positives: argument type if verified */
    array_map(function (string $value) { return trim($value); }, []);
    /* false-positives: arguments by reference */
    array_walk($array, function(&$value) { $value = trim($value); });

    array_map(<weak_warning descr="[EA] The closure can be replaced with 'strval' (reduces cognitive load).">function ($value) { return (string) $value; }</weak_warning>, []);
    array_map(function ($value) { return (string) trim($value); }, []);
    array_map(function ($value) { return (array) $value; }, []);
}