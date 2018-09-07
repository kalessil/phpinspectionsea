<?php

function cases_holder() {
    foreach ([] as $value) {}

    foreach ([] as & $value) {}

    foreach ([] as $value) {}

    /* @var $four string[] */
    $four = [];
    foreach ($four as $value) {}

    $five = require $file;
    foreach ($five as $value) {}
}