<?php

function cases_holder($array) {
    array_map('trim', $array);
    array_filter($array, 'is_numeric');
    array_walk($array, 'trim');
    array_walk_recursive($array, 'trim');
    array_walk_recursive($array, 'trim');

    array_map('\trim', $array);

    /* false-positives: argument type if verified */
    array_map(function (string $value) { return trim($value); }, []);
    /* false-positives: arguments by reference */
    array_walk($array, function(&$value) { $value = trim($value); });

    array_map('strval', []);
    array_map(function ($value) { return (string) trim($value); }, []);
    array_map(function ($value) { return (array) $value; }, []);
}