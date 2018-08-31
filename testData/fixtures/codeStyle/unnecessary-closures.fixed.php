<?php

function cases_holder() {
    array_map('trim', []);
    array_filter([], 'is_numeric');

    array_map(function (string $value) { return trim($value); }, []);
    array_filter([], function (string $value) { return is_numeric($value); });
}