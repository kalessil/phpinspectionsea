<?php

function cases_holder($array) {
    array_map(<weak_warning descr="[EA] The closure can be replaced with 'trim' (reduces cognitive load).">fn($value) => trim($value)</weak_warning>, $array);
    array_filter($array, <weak_warning descr="[EA] The closure can be replaced with 'is_numeric' (reduces cognitive load).">fn($value) => is_numeric($value)</weak_warning>);
    array_walk($array, <weak_warning descr="[EA] The closure can be replaced with 'trim' (reduces cognitive load).">fn($value) => trim($value)</weak_warning>);
    array_walk_recursive($array, <weak_warning descr="[EA] The closure can be replaced with 'trim' (reduces cognitive load).">fn($value) => trim($value)</weak_warning>);
    array_walk_recursive($array, <weak_warning descr="[EA] The closure can be replaced with 'is_null' (reduces cognitive load).">fn($value) => $value === null</weak_warning>);
    array_map(<weak_warning descr="[EA] The closure can be replaced with 'strval' (reduces cognitive load).">fn($value) => (string) $value</weak_warning>, []);
}