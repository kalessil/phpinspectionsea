<?php

    echo <weak_warning descr="PHP_SAPI constant should be used instead.">php_sapi_name()</weak_warning>;
    echo <weak_warning descr="PHP_VERSION constant should be used instead.">phpversion()</weak_warning>;
    echo <weak_warning descr="M_PI constant should be used instead.">pi()</weak_warning>;

    echo phpversion('extension-name');