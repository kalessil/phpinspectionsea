<?php

public function cases_holder() {
    <warning descr="Variable $array is redundant.">$array</warning> = [];
    foreach ($array as $value) {}

    <warning descr="Variable $array is redundant.">$array</warning> = [];
    foreach ($array as & $value) {}

    /* @var $source string[] */
    <warning descr="Variable $array is redundant.">$array</warning> = [];
    foreach ($array as $value) {}

    /* @var $array string[] */
    $array = [];
    foreach ($array as $value) {}

    $array = require $file;
    foreach ($array as $value) {}
}