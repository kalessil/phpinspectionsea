<?php

/* pre-defined $_ vars weights calculation: add 0 instead of 1 */
if ($_SERVER ['REQUEST_METHOD'] === 'POST' && array_key_exists('comment', $_POST)) {}

/* interconnected statements: array and array access */
$in = [];
if (is_array_indexed($in) && is_array($in[0])) {}

/* interconnected statements: assigned variable case */
if (($count = $counter->count()) && $count > 0) {}
if (($count = $i = $counter->count()) && $count > 0) {}

/* interconnected statements: leading isset case */
if (!isset($array[$index]) && !array_key_exists($index, $array)) {}
if (!isset($array[trim($index)]) && !array_key_exists($index, $array)) {}

/* older code samples: weights estimation */
if (isset($x) && $y)                     {}
if (empty($x) && $y)                     {}
if (isset($x[uniqid()]) && $y[uniqid()]) {}

/* duplicate calls: variable gets overridden */
if (!call($mixed)) {
    $mixed = fallback($mixed);
    if (!call($mixed)) {
        /* something else */
    }
}

/* array access properly calculation check */
if (isset($array[0]) || array_key_exists(0, $array)) {}