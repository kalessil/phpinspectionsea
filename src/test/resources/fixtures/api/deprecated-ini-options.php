<?php

    /* target functions */
    ini_set(<warning descr="'iconv.input_encoding' is a deprecated since PHP 5.6.0. Use default_charset instead.">'iconv.input_encoding'</warning>);
    ini_get(<warning descr="'iconv.input_encoding' is a deprecated since PHP 5.6.0. Use default_charset instead.">'iconv.input_encoding'</warning>);
    ini_alter(<warning descr="'iconv.input_encoding' is a deprecated since PHP 5.6.0. Use default_charset instead.">'iconv.input_encoding'</warning>);

    /* deprecations with and without alternatives */
    ini_restore(<warning descr="'iconv.input_encoding' is a deprecated since PHP 5.6.0. Use default_charset instead.">'iconv.input_encoding'</warning>);
    ini_restore(<warning descr="'always_populate_raw_post_data' is a deprecated since PHP 5.6.0.">'always_populate_raw_post_data'</warning>);

    /* removals with and without alternatives */
    ini_restore(<warning descr="'session.bug_compat_42' was removed in PHP 5.4.0.">'session.bug_compat_42'</warning>);
    ini_restore(<warning descr="'mbstring.script_encoding' was removed in PHP 5.4.0. Use zend.script_encoding instead.">'mbstring.script_encoding'</warning>);