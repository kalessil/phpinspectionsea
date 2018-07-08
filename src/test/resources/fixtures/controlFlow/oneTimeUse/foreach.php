<?php

function cases_holder() {
    <warning descr="Variable $one is redundant.">$one</warning> = [];
    foreach ($one as $value) {}

    <warning descr="Variable $two is redundant.">$two</warning> = [];
    foreach ($two as & $value) {}

    /* @var $source string[] */
    <warning descr="Variable $three is redundant.">$three</warning> = [];
    foreach ($three as $value) {}

    /* @var $four string[] */
    $four = [];
    foreach ($four as $value) {}

    $five = require $file;
    foreach ($five as $value) {}
}