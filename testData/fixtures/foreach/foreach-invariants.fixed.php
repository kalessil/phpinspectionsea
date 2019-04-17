<?php

namespace NS {
    class CasesHolder {
        public function casesHolder() {
            foreach ($arr as $iValue) {
                echo $iValue;
            }
            foreach ($arr as $v) {
                echo $v;
            }
        }

        public function casesForLimits($array, $limit) {
            foreach ($array as $iValue) {
                echo $iValue;
            }
            foreach ($array as $iValue) {
                echo $iValue;
            }
            foreach ($array as $iValue) {
                echo $iValue;
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
        foreach ($arr as $iValue) {
            echo $iValue, $iValue->property;
            $x = $iValue;
            $x = !$iValue;
            $x = trim($iValue);
            $x = $iValue > 0;
            $x = ($iValue) > 0;

            switch ($iValue) { case $iValue: break; }
            for ($iValue; $iValue; $iValue) {}
            while ($iValue) {}
            do {} while ($iValue);
            if ($iValue) {} elseif ($iValue) {}
        }
        foreach ($arr as $i => $iValue) {
            $iValue->property    = '...';
            $iValue->property[0] = '...';
            $arr[$i][0]           = '...';
            echo $iValue, $iValue->property;

            $x = & $arr[$i];
            $x = &$arr[$i];
            $x =& $arr[$i];
        }
        foreach ($arr as $iValue) {
            echo $iValue->property, $iValue->property[0], $iValue[0];
            echo $iValue, $iValue . " $iValue " . " $iValue ";
        }

        /* case: slow loop */
        foreach ($arr as $v) {
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