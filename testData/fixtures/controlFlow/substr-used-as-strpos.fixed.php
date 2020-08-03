<?php

function calls_cases_holder() {
    $x = strpos($path, $pathPrefix) === 0;
    $x = strpos($path, $pathPrefix) !== 0;
    $x = strpos($path, $pathPrefix) === 0;
    $x = strpos($path, $pathPrefix) !== 0;

    $x = mb_strpos($path, $pathPrefix) === 0;
    $x = mb_strpos($path, $pathPrefix, '') === 0;

    $x = stripos($path, $pathPrefix) === 0;
    $x = mb_stripos($path, $pathPrefix) === 0;
    $x = mb_stripos($path, $pathPrefix) === 0;

    /* false-positives: length is not as expected */
    $x = substr($path, 0, strrpos($path, '...')) == $pathPrefix;
}