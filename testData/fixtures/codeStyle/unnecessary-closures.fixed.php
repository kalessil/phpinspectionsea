<?php

function cases_holder($array) {
    array_map('trim', $array);
    array_filter($array, 'is_numeric');
    array_walk($array, 'trim');
    array_walk($array, 'trim');
    array_walk_recursive($array, 'trim');
    array_walk_recursive($array, 'trim');

    array_map('\trim', $array);

    array_map(function (string $value) { return trim($value); }, []);
}