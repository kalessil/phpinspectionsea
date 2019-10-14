<?php

    <error descr="[EA] Please ensure this is not a forgotten debug statement.">print_r($a)</error>;

    <error descr="[EA] Please ensure this is not a forgotten debug statement.">var_export($a)</error>;

    <error descr="[EA] Please ensure this is not a forgotten debug statement.">var_dump($a)</error>;
    <error descr="[EA] Please ensure this is not a forgotten debug statement.">var_dump($a, $b)</error>;

    <error descr="[EA] Please ensure this is not a forgotten debug statement.">error_log($a)</error>;
    <error descr="[EA] Please ensure this is not a forgotten debug statement.">error_log($a, 0)</error>;
    <error descr="[EA] Please ensure this is not a forgotten debug statement.">error_log($a, 0, '/tmp/debug.log')</error>;

    <error descr="[EA] Please ensure this is not a forgotten debug statement.">debug_zval_dump($a)</error>;
    <error descr="[EA] Please ensure this is not a forgotten debug statement.">debug_zval_dump($a, $b)</error>;

    <error descr="[EA] Please ensure this is not a forgotten debug statement.">debug_print_backtrace()</error>;
    <error descr="[EA] Please ensure this is not a forgotten debug statement.">phpinfo()</error>;

    <error descr="[EA] Please ensure this is not a forgotten debug statement.">my_debug_function()</error>;
