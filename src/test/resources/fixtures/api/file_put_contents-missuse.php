<?php

    <error descr="'copy($from, $to)' would consume less cpu and memory resources here">file_put_contents</error> ($to, file_get_contents($from));
    <error descr="'copy($from, $to)' would consume less cpu and memory resources here">file_put_contents</error> ($to, @file_get_contents($from));

    /* false-positives: extra flags */
    file_put_contents($to, file_get_contents($from), $flags);
    file_put_contents($to, file_get_contents($from, $useIncludePath));