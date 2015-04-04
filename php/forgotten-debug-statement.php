<?php

    $a = $b = array();

    print_r($a);
    echo print_r($a, true);

    var_export($a);
    echo var_export($a, true);

    var_dump($a);
    var_dump($a, $b);

    debug_zval_dump($a);
    debug_zval_dump($a, $b);
