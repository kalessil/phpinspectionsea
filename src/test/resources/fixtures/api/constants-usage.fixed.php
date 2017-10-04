<?php

    echo PHP_SAPI;
    echo PHP_VERSION;
    echo __CLASS__;
    echo M_PI;
    echo PHP_OS;

    echo phpversion('extension-name');
    echo phpversion($object);

    version_compare(PHP_VERSION, '7', '>=');
    version_compare(PHP_VERSION, '7.1', '>=');
    version_compare(PHP_VERSION, '7.1.11', '>=');
    version_compare(PHP_VERSION, '7.1.11-whatever', '>=');