<?php

    <error descr="Following construct should be used: 'if (!mkdir('...') && !is_dir('...')) { ... }'.">mkdir('...');</error>
    <error descr="Following construct should be used: 'if (!mkdir('...') && !is_dir('...')) { ... }'.">mkdir('...');</error>

    /* false-positive: result saved */
    $result = mkdir('...');


    if (!is_dir('...')) {
        <error descr="Following construct should be used: 'if (!mkdir('...') && !is_dir('...')) { ... }'.">mkdir('...');</error>
    }

    <error descr="Following construct should be used: 'if (!mkdir('...') && !is_dir('...')) { ... }'.">if</error> ((!mkdir('...'))) {}


    if (!is_dir('...') && <error descr="Condition needs to be corrected (invert if needed): '!mkdir('...') && !is_dir('...')'.">!mkdir('...')</error>) {}

    /* false-positive: re-checked afterwards */
    if (!is_dir('...') && !mkdir('...') && !is_dir('...')) {}