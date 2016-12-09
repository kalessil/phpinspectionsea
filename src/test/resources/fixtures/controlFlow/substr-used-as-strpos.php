<?php

    $x = <weak_warning descr="'0 === strpos($path, $pathPrefix)' can be used instead (improves maintainability)">substr($path, 0, strlen($pathPrefix)) == $pathPrefix</weak_warning>;
    $x = <weak_warning descr="'0 !== strpos($path, $pathPrefix)' can be used instead (improves maintainability)">substr($path, 0, strlen($pathPrefix)) != $pathPrefix</weak_warning>;
    $x = <weak_warning descr="'0 === strpos($path, $pathPrefix)' can be used instead (improves maintainability)">substr($path, 0, strlen($pathPrefix)) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="'0 !== strpos($path, $pathPrefix)' can be used instead (improves maintainability)">substr($path, 0, strlen($pathPrefix)) !== $pathPrefix</weak_warning>;

    $x = <weak_warning descr="'0 === mb_strpos($path, $pathPrefix)' can be used instead (improves maintainability)">mb_substr($path, 0, mb_strlen($pathPrefix)) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="'0 === mb_strpos($path, $pathPrefix, '')' can be used instead (improves maintainability)">mb_substr($path, 0, mb_strlen($pathPrefix), '') === $pathPrefix</weak_warning>;

    $x = <weak_warning descr="'0 === stripos($path, $pathPrefix)' can be used instead (improves maintainability)">strtoupper(substr($path, 0, strlen($pathPrefix))) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="'0 === mb_stripos($path, $pathPrefix)' can be used instead (improves maintainability)">strtoupper(mb_substr($path, 0, strlen($pathPrefix))) === $pathPrefix</weak_warning>;
    $x = <weak_warning descr="'0 === mb_stripos($path, $pathPrefix)' can be used instead (improves maintainability)">strtolower(mb_substr($path, 0, strlen($pathPrefix))) === $pathPrefix</weak_warning>;
