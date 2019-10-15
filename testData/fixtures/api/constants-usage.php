<?php

    use function get_class;

    echo <weak_warning descr="[EA] PHP_SAPI constant should be used instead.">php_sapi_name()</weak_warning>;
    echo <weak_warning descr="[EA] PHP_VERSION constant should be used instead.">phpversion()</weak_warning>;
    echo <weak_warning descr="[EA] __CLASS__ constant should be used instead.">get_class()</weak_warning>;
    echo <weak_warning descr="[EA] M_PI constant should be used instead.">pi()</weak_warning>;
    echo <weak_warning descr="[EA] PHP_OS constant should be used instead.">php_uname('s')</weak_warning>;

    echo phpversion('extension-name');
    echo phpversion($object);
    echo php_uname('...');
    echo php_uname();

    <weak_warning descr="[EA] Consider using 'PHP_VERSION_ID >= 70000' instead.">version_compare(PHP_VERSION, '7', '>=')</weak_warning>;
    <weak_warning descr="[EA] Consider using 'PHP_VERSION_ID === 70100' instead.">version_compare(PHP_VERSION, '7.1', '==')</weak_warning>;
    <weak_warning descr="[EA] Consider using 'PHP_VERSION_ID !== 70111' instead.">version_compare(PHP_VERSION, '7.1.11', '!=')</weak_warning>;

    version_compare(PHP_VERSION, '7.1.11-whatever', '>=');