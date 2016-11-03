<?php

    echo var_export($a, true);
    echo print_r($a, true);

    ob_start();
    print_r($a);

    ob_start();
    @print_r($a);