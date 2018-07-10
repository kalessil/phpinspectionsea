<?php

    $x = <weak_warning descr="'strpos($path, $pathPrefix) === 0' can be used instead (improves maintainability).">substr($path, 0, strlen($pathPrefix)) == $pathPrefix</weak_warning>;
    $x = <weak_warning descr="'strpos($path, $pathPrefix) !== 0' can be used instead (improves maintainability).">substr($path, 0, strlen($pathPrefix)) != $pathPrefix</weak_warning>;
    $x = <weak_warning descr="'strpos($path, $pathPrefix) === 0' can be used instead (improves maintainability).">substr($path, 0, strlen($pathPrefix)) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="'strpos($path, $pathPrefix) !== 0' can be used instead (improves maintainability).">substr($path, 0, strlen($pathPrefix)) !== $pathPrefix</weak_warning>;

    $x = <weak_warning descr="'mb_strpos($path, $pathPrefix) === 0' can be used instead (improves maintainability).">mb_substr($path, 0, mb_strlen($pathPrefix)) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="'mb_strpos($path, $pathPrefix, '') === 0' can be used instead (improves maintainability).">mb_substr($path, 0, mb_strlen($pathPrefix), '') === $pathPrefix</weak_warning>;

    $x = <weak_warning descr="'stripos($path, $pathPrefix) === 0' can be used instead (improves maintainability).">strtoupper(substr($path, 0, strlen($pathPrefix))) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="'mb_stripos($path, $pathPrefix) === 0' can be used instead (improves maintainability).">strtoupper(mb_substr($path, 0, strlen($pathPrefix))) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="'mb_stripos($path, $pathPrefix) === 0' can be used instead (improves maintainability).">strtolower(mb_substr($path, 0, strlen($pathPrefix))) === $pathPrefix</weak_warning>;
