<?php

function cases_holder() {
    array_map(<weak_warning descr="The closure can be replaced with 'trim' (reduces cognitive load).">function ($value) { return trim($value); }</weak_warning>, []);
    array_filter([], <weak_warning descr="The closure can be replaced with 'is_numeric' (reduces cognitive load).">function ($value) { return is_numeric($value); }</weak_warning>);

    array_map(function (string $value) { return trim($value); }, []);
    array_filter([], function (string $value) { return is_numeric($value); });
}