<?php

    $arr = array();

    // shall be reported - index access is used
    <warning descr="Foreach can probably be used instead (easier to read and support; ensure a string is not iterated)">for</warning> ($i = 0, $max = count($arr); $i < $max; ++$i, $z = 0) {
        echo $arr[$i];
    }
    <warning descr="Foreach can probably be used instead (easier to read and support; ensure a string is not iterated)">for</warning> ($i = 0, $max = count($arr); $i < $max; $i++, $z = 0) {
        echo $arr[$i];
    }

    // shall be reported - slow loop
    <error descr="Foreach should be used instead (8x faster)">while</error> (list($k, $v) = each($arr)) {
        echo $v;
    }