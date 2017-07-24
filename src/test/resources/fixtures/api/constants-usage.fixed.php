<?php

    echo PHP_SAPI;
    echo PHP_VERSION;
    echo __CLASS__;
    echo M_PI;
    // echo [weak_warning descr="PHP_OS constant should be used instead."]php_uname('s')[/weak_warning>];

    echo phpversion('extension-name');
    echo phpversion($object);