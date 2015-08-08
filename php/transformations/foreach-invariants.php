<?php

    $arr = array();
    // shall be reported - index access is used
    for ($i = 0, $max = count($arr); $i < $max; ++$i, $z = 0) {
        echo $arr[$i];
    }
    for ($i = 0, $max = count($arr); $i < $max; $i++, $z = 0) {
        echo $arr[$i];
    }

    // shall not be reported as no index access
    for ($i = 0, $max = count($arr); $i < $max; ++$i, $z = 0) {
        echo $i;
    }
    for ($i = 0; $i < 10; $i++, $z = 0) {
        echo $arr[$i];
    }