<?php

// see https://bugs.php.net/bug.php?id=70944
$e = new \RuntimeException('Bar');
try {
    throw new \RuntimeException('Foo', 0, $e);
} catch (\Exception $ex) {
    /* do nothing */
} finally {
    try {

    } catch (\Exception $ex) {

    }

    throw $e;
}