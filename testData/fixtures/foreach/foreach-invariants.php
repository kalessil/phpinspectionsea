<?php

namespace NS {
    class CasesHolder {
        public function casesHolder() {
            $iMax = count($arr);
            <warning descr="Foreach can probably be used instead (easier to read and support).">for</warning> ($i = 0; $i < $iMax; $i++, $z = 0) {
                echo $arr[$i];
            }

            <error descr="Foreach should be used instead (8x faster).">while</error> (list($i, $v) = each($arr)) {
                echo $arr[$i];
            }
        }
    }
}

namespace {
    function cases_holder($arr) {
        /* case: invariant */
        <warning descr="Foreach can probably be used instead (easier to read and support).">for</warning> ($i = 0, $max = count($arr); $i < $max; ++$i, $z = 0) {
            echo $arr[$i], $arr[$i]->property;
            $x = $arr[$i];
            $x = !$arr[$i];
            $x = trim($arr[$i]);
            $x = $arr[$i] > 0;
            $x = ($arr[$i]) > 0;
        }
        <warning descr="Foreach can probably be used instead (easier to read and support).">for</warning> ($i = 0, $max = count($arr); $i < $max; $i++, $z = 0) {
            echo $arr[$i], $arr[$i]->property;
            $x = & $arr[$i];
            $x = &$arr[$i];
            $x =& $arr[$i];
        }
        $iMax = count($arr);
        <warning descr="Foreach can probably be used instead (easier to read and support).">for</warning> ($i = 0; $i < $iMax; $i++, $z = 0) {
            echo $arr[$i]->property, $arr[$i]->property[0], $arr[$i][0];
            echo $arr[$i], $arr[$i] . " $arr[$i] " . " {$arr[$i]} ";
        }

        /* case: slow loop */
        <error descr="Foreach should be used instead (8x faster).">while</error> (list($i, $v) = each($arr)) {
            echo $arr[$i]->property;
        }
    }

    function false_positives_holder($arr) {
        /* false-positives: complex repetitive checks */
        for ($i = 0, $max = count($arr); $i < $max || $condition; ++$i, $z = 0) {
            $condition = $arr[$i] % 0;
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

        /* false-positives: string */
        $string = '...';
        for ($i = 0, $max = strlen($string); $i < $max; ++$i) {
            echo $string[$i];
        }
    }
}