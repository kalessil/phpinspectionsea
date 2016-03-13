<?php

$obj1 = new stdClass();
$obj2 = new stdClass();

if (isset($obj1, $obj2) && null !== $obj1 && $obj2 !== null) {
    echo 'Object';
}