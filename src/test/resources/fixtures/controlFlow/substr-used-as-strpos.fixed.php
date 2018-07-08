<?php

    $x = strpos($path, $pathPrefix) === 0;
    $x = strpos($path, $pathPrefix) !== 0;
    $x = strpos($path, $pathPrefix) === 0;
    $x = strpos($path, $pathPrefix) !== 0;

    $x = mb_strpos($path, $pathPrefix) === 0;
    $x = mb_strpos($path, $pathPrefix, '') === 0;

    $x = stripos($path, $pathPrefix) === 0;
    $x = mb_stripos($path, $pathPrefix) === 0;
    $x = mb_stripos($path, $pathPrefix) === 0;
