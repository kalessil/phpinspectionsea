<?php

    $a = $b = array();

    print_r($a);               // <- reported
    echo print_r($a, true);

    var_export($a);            // <- reported
    echo var_export($a, true);

    var_dump($a);              // <- reported
    var_dump($a, $b);          // <- reported

    debug_zval_dump($a);       // <- reported
    debug_zval_dump($a, $b);   // <- reported

    debug_print_backtrace();   // <- reported

    ob_start();
    print_r($a);               // <- NOT reported
