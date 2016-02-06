<?php

    $x = substr($path, 0, strlen($pathPrefix)) == $pathPrefix;
    $x = substr($path, 0, strlen($pathPrefix)) != $pathPrefix;
    $x = substr($path, 0, strlen($pathPrefix)) === $pathPrefix;
    $x = substr($path, 0, strlen($pathPrefix)) !== $pathPrefix;