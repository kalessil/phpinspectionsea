<?php

function cases_holder() {
    <warning descr="Variable $one is redundant.">$one</warning> = [];
    foreach ($one as $value) {}

    $two = [];
    foreach ($two as & $value) {}

    /* @var $source string[] */
    <warning descr="Variable $three is redundant.">$three</warning> = [];
    foreach ($three as $value) {}

    /* @var $four string[] */
    $four = [];
    foreach ($four as $value) {}

    $five = require $file;
    foreach ($five as $value) {}

    foreach ([] as $loopVariable) {
        <warning descr="The local variable introduction doesn't make much sense here, consider renaming a loop variable instead.">$localVariable</warning> = $loopVariable;
        $result = $source[$localVariable];
    }
}