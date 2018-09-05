<?php

    use function get_class;

    echo PHP_SAPI;
    echo PHP_VERSION;
    echo __CLASS__;
    echo M_PI;
    echo PHP_OS;

    echo phpversion('extension-name');
    echo phpversion($object);

    PHP_VERSION_ID >= 70000;
    PHP_VERSION_ID === 70100;
    PHP_VERSION_ID !== 70111;

    version_compare(PHP_VERSION, '7.1.11-whatever', '>=');