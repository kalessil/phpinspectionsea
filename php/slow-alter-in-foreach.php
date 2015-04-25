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
