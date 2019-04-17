<?php

namespace NS {
    class CasesHolder {
        public function casesHolder() {
            $iMax = count($arr);
            <warning descr="Foreach can probably be used instead (easier to read and support).">for</warning> ($i = 0; $i < $iMax; $i++) {
                echo $arr[$i];
            }
            <error descr="Foreach should be used instead (8x faster, also deprecated since PHP 7.2).">while</error> (list($i, $v) = each($arr)) {
                echo $v;
            }
        }

        public function casesForLimits($array, $limit) {
            $limitExternal = count($array);
            <warning descr="Foreach can probably be used instead (easier to read and support).">for</warning> ($i = 0; $i < $limitExternal; $i++) {
                echo $array[$i];
            }
            <warning descr="Foreach can probably be used instead (easier to read and support).">for</warning> ($i = 0, $limitInitialized = count($array); $i < $limitInitialized; $i++) {
                echo $array[$i];
            }
            <warning descr="Foreach can probably be used instead (easier to read and support).">for</warning> ($i = 0; $i < count($array); $i++) {
                echo $array[$i];
            }
            for ($i = 0; $i < $limit; $i++) {
                echo $array[$i];
            }
        }

        public function falsePositives() {
            while (list($i, $v) = each($arr)) {
                echo $arr[$i];
            }
        }
    }
}

namespace {
    function cases_holder($arr) {
        /* case: invariant */
        <warning descr="Foreach can probably be used instead (easier to read and support).">for</warning> ($i = 0, $max1 = count($arr); $i < $max1; ++$i) {
            echo $arr[$i], $arr[$i]->property;
            $x = $arr[$i];
            $x = !$arr[$i];
            $x = trim($arr[$i]);
            $x = $arr[$i] > 0;
            $x = ($arr[$i]) > 0;
        }
        <warning descr="Foreach can probably be used instead (easier to read and support).">for</warning> ($i = 0, $max2 = count($arr); $i < $max2; $i++) {
            $arr[$i]->property    = '...';
            $arr[$i]->property[0] = '...';
            $arr[$i][0]           = '...';
            echo $arr[$i], $arr[$i]->property;

            $x = & $arr[$i];
            $x = &$arr[$i];
            $x =& $arr[$i];
        }
        $max3 = count($arr);
        <warning descr="Foreach can probably be used instead (easier to read and support).">for</warning> ($i = 0; $i < $max3; $i++) {
            echo $arr[$i]->property, $arr[$i]->property[0], $arr[$i][0];
            echo $arr[$i], $arr[$i] . " $arr[$i] " . " {$arr[$i]} ";
        }

        /* case: slow loop */
        <error descr="Foreach should be used instead (8x faster, also deprecated since PHP 7.2).">while</error> (list($i, $v) = each($arr)) {
            echo $v->property;
        }
    }

    function false_positives_holder($arr) {
        /* false-positives: complex repetitive checks */
        for ($i = 0, $max = count($arr); $i < $max || $condition; ++$i) {
            $condition = $arr[$i] % 0;
        }

        /* false-positives: multiple repetitive statements */
        for ($i = 0, $max = count($arr); $i < $max; ++$i) {
            echo $arr[$i];
        }

        /* false-positives: no subject index based access */
        for ($i = 0, $max = count($arr); $i < $max; ++$i) {
            echo $i;
        }
        for ($max = count($arr), $i = 0; $i < $max; $i++) {
            echo $i;
        }
        for ($i = 0; $i < 10; $i++) {
            echo $arr[$i];
        }

        /* false-positives: multiple containers */
        $col = array();
        for ($i = 0, $max = count($arr); $i < $max; $i++) {
            echo $arr[$i], $col[$i];
        }

        /* false-positives: string */
        $string = '...';
        for ($i = 0, $max = strlen($string); $i < $max; ++$i) {
            echo $string[$i];
        }
    }
}