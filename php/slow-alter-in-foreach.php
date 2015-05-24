<?php

    $arrPool = array();

    foreach ($arrPool as $key => $value) {
        /* warn bout using reference */
        $arrPool[$key] = (int) $value;
    }
    /* following unset is ambiguous */
    unset($value);

    /* shall warn about missing unset and runtime fatal */
    foreach ($arrPool as & $key => &$value) {
        $value = (float) $value;
    }
    //unset($value);
    unset($key);


    function foo ($x) {
        if (is_array($x)) {
            foreach ($x as &$y) {
                ++$y;
            }
        } elseif ($x) {
            ++$x;
        }

        return $x;
    }

    function bar ($x) {
        if (is_array($x)) {
            if (is_array($x)) {
                foreach ($x as &$y) {
                    ++$y;
                }
            }
        }
    }
