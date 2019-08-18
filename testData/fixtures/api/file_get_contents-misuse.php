<?php

    <error descr="'copy($from, $to)' would consume less cpu and memory resources here.">file_put_contents($to, file_get_contents($from))</error>;
    <error descr="'copy($from, $to)' would consume less cpu and memory resources here.">file_put_contents($to, @file_get_contents($from))</error>;
    <error descr="'md5_file($from)' would consume less cpu and memory resources here.">md5(file_get_contents($from))</error>;
    <error descr="'sha1_file($from)' would consume less cpu and memory resources here.">sha1(file_get_contents($from))</error>;

    /* false-positives: extra flags */
    file_put_contents($to, file_get_contents($from), $flags);
    file_put_contents($to, file_get_contents($from, $useIncludePath));

    /* false-positives: methods */
    file_put_contents($to, $x->file_get_contents($from));
    $x->file_put_contents($to, file_get_contents($from));