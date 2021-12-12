<?php

/* pre-defined $_ vars weights calculation: add 0 instead of 1 */
if ($_SERVER ['REQUEST_METHOD'] === 'POST' && array_key_exists('comment', $_POST)) {}

/* interconnected statements: array and array access */
$in = [];
if (is_array_indexed($in) && is_array($in[0])) {}

/* interconnected statements: assigned variable case */
if (($count = $counter->count()) && $count) {}
if (($count = $counter->count()) && $count > 0) {}
if (($count = $i = $counter->count()) && $count > 0) {}

/* interconnected statements: parameters by reference */
if (($first = array_shift($array)) && $array) {}

/* interconnected statements: leading isset case */
if (!isset($array[$index]) && !array_key_exists($index, $array)) {}
if (!isset($array[trim($index)]) && !array_key_exists($index, $array)) {}

/* older code samples: weights estimation */
if (isset($x) && $y)                         {}
if (!isset($x) && $y)                        {}
if (isset($x[uniqid()]) && $y[uniqid()])     {}
if (isset($x[$key]) && $y[$key])             {}
if (empty($x) && $y)                         {}
if (!empty($x) && $y)                        {}
if (empty($x[$key]) && $y[$key])             {}
if (is_null($x) && $y)                       {}
if (!is_null($x) && $y)                      {}
if (array_key_exists($key, $y) && $y[$key])  {}
if (!array_key_exists($key, $y) && $y[$key]) {}
if (function_exists('f') && f())             {}
if (method_exists($y, 'm') && $y->m())       {}
if (property_exists($y, 'p') && $y->p)       {}
if (class_exists('Clazz') && $y->p)          {}
if (interface_exists('Contract') && $y->p)   {}
if (trait_exists('CopyPaste') && $y->p)      {}
if (is_array($x) && $y)                      {}
if (is_object($x) && $y)                     {}
if (is_bool($x) && $y)                       {}
if (is_int($x) && $y)                        {}
if (is_integer($x) && $y)                    {}
if (is_long($x) && $y)                       {}
if (is_float($x) && $y)                      {}
if (is_double($x) && $y)                     {}
if (is_real($x) && $y)                       {}
if (is_numeric($x) && $y)                    {}
if (is_string($x) && $y)                     {}
if (is_scalar($x) && $y)                     {}
if (is_callable($x) && $y)                   {}
if ($x instanceof Clazz && $y)               {}
if (is_a($x, Clazz::class) && $y)            {}
if (is_subclass_of($x, Clazz::class) && $y)  {}

/* duplicate calls: variable gets overridden */
if (!call($mixed)) {
    $mixed = fallback($mixed);
    if (!call($mixed)) {
        /* something else */
    }
}

/* array access properly calculation check */
if (isset($array[0]) || array_key_exists(0, $array)) {}

/* variable property/variable names */
if ($object->getString() && $object->{$object->getString()}) {}
if ($object->getString() && "{$object->getString()}") {}
if ($object->getString() && ${$object->getString()}) {}