<?php

function calls_cases_holder() {
    $x = <weak_warning descr="[EA] 'strpos($path, $pathPrefix) === 0' can be used instead (improves maintainability; the replacement is a commonly used invariant).">substr($path, 0, strlen($pathPrefix)) == $pathPrefix</weak_warning>;
    $x = <weak_warning descr="[EA] 'strpos($path, $pathPrefix) !== 0' can be used instead (improves maintainability; the replacement is a commonly used invariant).">substr($path, 0, strlen($pathPrefix)) != $pathPrefix</weak_warning>;
    $x = <weak_warning descr="[EA] 'strpos($path, $pathPrefix) === 0' can be used instead (improves maintainability; the replacement is a commonly used invariant).">substr($path, 0, strlen($pathPrefix)) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="[EA] 'strpos($path, $pathPrefix) !== 0' can be used instead (improves maintainability; the replacement is a commonly used invariant).">substr($path, 0, strlen($pathPrefix)) !== $pathPrefix</weak_warning>;

    $x = <weak_warning descr="[EA] 'mb_strpos($path, $pathPrefix) === 0' can be used instead (improves maintainability; the replacement is a commonly used invariant).">mb_substr($path, 0, mb_strlen($pathPrefix)) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="[EA] 'mb_strpos($path, $pathPrefix, '') === 0' can be used instead (improves maintainability; the replacement is a commonly used invariant).">mb_substr($path, 0, mb_strlen($pathPrefix), '') === $pathPrefix</weak_warning>;

    $x = <weak_warning descr="[EA] 'stripos($path, $pathPrefix) === 0' can be used instead (improves maintainability; the replacement is a commonly used invariant).">strtoupper(substr($path, 0, strlen($pathPrefix))) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="[EA] 'mb_stripos($path, $pathPrefix) === 0' can be used instead (improves maintainability; the replacement is a commonly used invariant).">strtoupper(mb_substr($path, 0, strlen($pathPrefix))) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="[EA] 'mb_stripos($path, $pathPrefix) === 0' can be used instead (improves maintainability; the replacement is a commonly used invariant).">strtolower(mb_substr($path, 0, strlen($pathPrefix))) === $pathPrefix</weak_warning>;

    /* false-positives: length is not as expected */
    $x = substr($path, 0, strrpos($path, '...')) == $pathPrefix;
}