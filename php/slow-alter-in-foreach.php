<?php

    $arrPool = array();

    foreach ($arrPool as $key => $value) {
        /** warn bout using reference */
        $arrPool[$key] = (int) $value;
    }

    /** shall warn about missing unset */
    foreach ($arrPool as $key => &$value) {
        $value = (float) $value;
    }
    //unset($value);
