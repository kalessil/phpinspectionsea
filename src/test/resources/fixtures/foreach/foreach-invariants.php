<?php

    $arr = array();

    function cases_holder($arr) {
        /* case: invariant */
        <warning descr="Foreach can probably be used instead (easier to read and support; ensure a string is not iterated).">for</warning> ($i = 0, $max = count($arr); $i < $max; ++$i, $z = 0) {
            echo $arr[$i], $arr[$i]->property;
            $x = $arr[$i];
            $x = $arr[$i] > 0;
        }
        <warning descr="Foreach can probably be used instead (easier to read and support; ensure a string is not iterated).">for</warning> ($i = 0, $max = count($arr); $i < $max; $i++, $z = 0) {
            echo $arr[$i], $arr[$i]->property;
            $x = & $arr[$i];
            $x = &$arr[$i];
            $x =& $arr[$i];
        }

        /* case: slow loop */
        <error descr="Foreach should be used instead (8x faster).">while</error> (list($k, $v) = each($arr)) {}
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