<?php

    $x = 0 === strpos($path, $pathPrefix);
    $x = 0 !== strpos($path, $pathPrefix);
    $x = 0 === strpos($path, $pathPrefix);
    $x = 0 !== strpos($path, $pathPrefix);

    $x = 0 === mb_strpos($path, $pathPrefix);
    $x = 0 === mb_strpos($path, $pathPrefix, '');

    $x = 0 === stripos($path, $pathPrefix);
    $x = 0 === mb_stripos($path, $pathPrefix);
    $x = 0 === mb_stripos($path, $pathPrefix);
