<?php

function cases_holder() {
    /* case: just a call -> conditionally throw exception */
    <error descr="Following construct should be used: 'if (!mkdir('...') && !is_dir('...')) { ... }'.">mkdir('...');</error>
    <error descr="Following construct should be used: 'if (!mkdir('...') && !is_dir('...')) { ... }'.">@mkdir('...');</error>
    if (!is_dir('...')) {
        <error descr="Following construct should be used: 'if (!mkdir('...') && !is_dir('...')) { ... }'.">mkdir('...');</error>
    }

    /* false-positive: result saved */
    $result = mkdir('...');

    /* case: incomplete conditions */
    if (<error descr="Some check are missing: 'mkdir('...') || is_dir('...')'.">(mkdir('...'))</error>) {}
    if (<error descr="Some check are missing: '!mkdir('...') && !is_dir('...')'.">(!mkdir('...'))</error>) {}
    if (<error descr="Some check are missing: '!mkdir('...') && !is_dir('...')'.">(!@mkdir('...'))</error>) {}
    if (!is_dir('...') && <error descr="Some check are missing: '!mkdir('...') && !is_dir('...')'.">!mkdir('...')</error>) {}
    if (is_dir('...') || <error descr="Some check are missing: 'mkdir('...') || is_dir('...')'.">mkdir('...')</error>) {}

    /* false-positive: re-checked afterwards */
    if (!is_dir('...') && !mkdir('...') && !is_dir('...')) {}
    if (is_dir('...') || mkdir('...') || is_dir('...')) {}
}