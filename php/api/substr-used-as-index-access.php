<?php

    $string = '0123456789';
    $position = 8;

    echo substr($string, $position, 1).PHP_EOL; // <- reported
    echo $string[$position].PHP_EOL;

    echo substr($string, $position, -1).PHP_EOL; // <- reported
    echo $string[$position].PHP_EOL;
