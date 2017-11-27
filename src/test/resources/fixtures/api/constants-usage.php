<?php

    use function get_class;

    echo <weak_warning descr="PHP_SAPI constant should be used instead.">php_sapi_name()</weak_warning>;
    echo <weak_warning descr="PHP_VERSION constant should be used instead.">phpversion()</weak_warning>;
    echo <weak_warning descr="__CLASS__ constant should be used instead.">get_class()</weak_warning>;
    echo <weak_warning descr="M_PI constant should be used instead.">pi()</weak_warning>;
    echo <weak_warning descr="PHP_OS constant should be used instead.">php_uname('s')</weak_warning>;

    echo phpversion('extension-name');
    echo phpversion($object);

    <weak_warning descr="'PHP_VERSION_ID >= 70000' should be used instead.">version_compare(PHP_VERSION, '7', '>=')</weak_warning>;
    <weak_warning descr="'PHP_VERSION_ID === 70100' should be used instead.">version_compare(PHP_VERSION, '7.1', '==')</weak_warning>;
    <weak_warning descr="'PHP_VERSION_ID !== 70111' should be used instead.">version_compare(PHP_VERSION, '7.1.11', '!=')</weak_warning>;

    version_compare(PHP_VERSION, '7.1.11-whatever', '>=');