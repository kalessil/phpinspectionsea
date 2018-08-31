<?php

    echo uniqid();
    echo \uniqid();

    echo in_array('', [], true);
    echo \in_array('', [], true);

    echo PHP_INT_MAX;
    echo \PHP_INT_MAX;

    \define('', '');
    define('', '');