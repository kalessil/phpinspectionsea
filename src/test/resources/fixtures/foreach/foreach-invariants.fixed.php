<?php

    $arr = array();

    function cases_holder($arr) {
        /* case: invariant */
        foreach ($arr as $i => $iValue) {
            echo $arr[$i], $iValue->property;
            $x = $iValue;
            $x = $iValue > 0;
        }
        foreach ($arr as $i => $iValue) {
            echo $arr[$i], $iValue->property;
            $x = & $arr[$i];
            $x = &$arr[$i];
            $x =& $arr[$i];
        }
        foreach ($arr as $iValue) {
            echo $iValue->property;
        }

        /* case: slow loop */
        while (list($k, $v) = each($arr)) {}
    }

    /* false-positives: no subject index based access */
    for ($i = 0, $max = count($arr); $i < $max; ++$i, $z = 0) {
        echo $i;
    }
    for ($max = count($arr), $i = 0; $i < $max; $i++, $z = 0) {
        echo $i;
    }
    for ($i = 0; $i < 10; $i++, $z = 0) {
        echo $arr[$i];
    }

    /* false-positives: multiple containers */
    $col = array();
    for ($i = 0, $max = count($arr); $i < $max; $i++, $z = 0) {
        echo $arr[$i], $col[$i];
    }