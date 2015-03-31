<?php

$obj = new stdClass();

if ($obj === null && null !== $obj && $obj instanceof stdClass) {
    echo 'Object';
}