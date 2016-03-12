<?php

    $x = substr($path, 0, strlen($pathPrefix)) == $pathPrefix;  // <- reported
    $x = substr($path, 0, strlen($pathPrefix)) != $pathPrefix;  // <- reported
    $x = substr($path, 0, strlen($pathPrefix)) === $pathPrefix; // <- reported
    $x = substr($path, 0, strlen($pathPrefix)) !== $pathPrefix; // <- reported