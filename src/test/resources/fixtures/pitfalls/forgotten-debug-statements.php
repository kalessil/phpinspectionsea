<?php

    <error descr="Please ensure this is not a forgotten debug statement.">print_r</error>($a);

    <error descr="Please ensure this is not a forgotten debug statement.">var_export</error>($a);

    <error descr="Please ensure this is not a forgotten debug statement.">var_dump</error>($a);
    <error descr="Please ensure this is not a forgotten debug statement.">var_dump</error>($a, $b);

    <error descr="Please ensure this is not a forgotten debug statement.">error_log</error>($a);
    <error descr="Please ensure this is not a forgotten debug statement.">error_log</error>($a, 0);
    <error descr="Please ensure this is not a forgotten debug statement.">error_log</error>($a, 0, '/tmp/debug.log');

    <error descr="Please ensure this is not a forgotten debug statement.">debug_zval_dump</error>($a);
    <error descr="Please ensure this is not a forgotten debug statement.">debug_zval_dump</error>($a, $b);

    <error descr="Please ensure this is not a forgotten debug statement.">debug_print_backtrace</error>();
    <error descr="Please ensure this is not a forgotten debug statement.">phpinfo</error>();
