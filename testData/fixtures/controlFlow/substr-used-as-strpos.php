<?php

function calls_cases_holder() {
    $x = <weak_warning descr="[EA] 'strpos($path, $pathPrefix) === 0' can be used instead (improves maintainability).">substr($path, 0, strlen($pathPrefix)) == $pathPrefix</weak_warning>;
    $x = <weak_warning descr="[EA] 'strpos($path, $pathPrefix) !== 0' can be used instead (improves maintainability).">substr($path, 0, strlen($pathPrefix)) != $pathPrefix</weak_warning>;
    $x = <weak_warning descr="[EA] 'strpos($path, $pathPrefix) === 0' can be used instead (improves maintainability).">substr($path, 0, strlen($pathPrefix)) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="[EA] 'strpos($path, $pathPrefix) !== 0' can be used instead (improves maintainability).">substr($path, 0, strlen($pathPrefix)) !== $pathPrefix</weak_warning>;

    $x = <weak_warning descr="[EA] 'mb_strpos($path, $pathPrefix) === 0' can be used instead (improves maintainability).">mb_substr($path, 0, mb_strlen($pathPrefix)) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="[EA] 'mb_strpos($path, $pathPrefix, '') === 0' can be used instead (improves maintainability).">mb_substr($path, 0, mb_strlen($pathPrefix), '') === $pathPrefix</weak_warning>;

    $x = <weak_warning descr="[EA] 'stripos($path, $pathPrefix) === 0' can be used instead (improves maintainability).">strtoupper(substr($path, 0, strlen($pathPrefix))) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="[EA] 'mb_stripos($path, $pathPrefix) === 0' can be used instead (improves maintainability).">strtoupper(mb_substr($path, 0, strlen($pathPrefix))) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="[EA] 'mb_stripos($path, $pathPrefix) === 0' can be used instead (improves maintainability).">strtolower(mb_substr($path, 0, strlen($pathPrefix))) === $pathPrefix</weak_warning>;

    /* false-positives: length is not as expected */
    $x = substr($path, 0, strrpos($path, '...')) == $pathPrefix;
}

function array_access_cases(string $string, array $array) {
    $x = <weak_warning descr="[EA] 'strpos($string, '.') === 0' can be used instead (improves maintainability).">$string[0] == '.'</weak_warning>;
    $x = <weak_warning descr="[EA] 'strpos($string, \".\") === 0' can be used instead (improves maintainability).">$string[0] === "."</weak_warning>;

    $x = $string[0] === '..';
    $x = $string[1] === '.';

    $x = $array[0] === '.';
}