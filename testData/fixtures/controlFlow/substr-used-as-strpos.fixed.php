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
}

function array_access_cases(string $string, array $array) {
    $x = strpos($string, '.') === 0;
    $x = strpos($string, ".") === 0;

    $x = $string[0] === '..';
    $x = $string[1] === '.';

    $x = $array[0] === '.';
}