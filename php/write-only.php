<?php

function callableWithWritesOnly(array $dx, array $dy)
{
    $dx = array();
    $dy = array();
    $dz = array();
    for ($i = 0; $i < 10; ++$i) {
        $dx[$i] = $i;
        $dy[$i] = $i;
        $dz[$i] = $i;
    }
}