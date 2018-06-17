<?php

function cases_holder() {
    /* case: just a call -> conditionally throw exception */
    <error descr="Following construct should be used: 'if (!mkdir('...', 0644) && !is_dir(...)) { ... }'.">mkdir('...', 0644);</error>
    <error descr="Following construct should be used: 'if (!mkdir('...', 0644) && !is_dir(...)) { ... }'.">@mkdir('...', 0644);</error>
    if (!is_dir('...')) {
        <error descr="Following construct should be used: 'if (!mkdir('...', 0644) && !is_dir(...)) { ... }'.">mkdir('...', 0644);</error>
    }

    /* false-positive: result saved */
    $result = mkdir('...');

    /* case: incomplete conditions */
    if (<error descr="Some check are missing: 'mkdir('...', 0644) || is_dir(...)'.">(mkdir('...', 0644))</error>) {}
    if (<error descr="Some check are missing: '!mkdir('...', 0644) && !is_dir(...)'.">(!mkdir('...', 0644))</error>) {}
    if (<error descr="Some check are missing: '!mkdir('...', 0644) && !is_dir(...)'.">(!@mkdir('...', 0644))</error>) {}
    if (!is_dir('...') && <error descr="Some check are missing: '!mkdir('...', 0644) && !is_dir(...)'.">!mkdir('...', 0644)</error>) {}
    if (is_dir('...') || <error descr="Some check are missing: 'mkdir('...', 0644) || is_dir(...)'.">mkdir('...', 0644)</error>) {}

    /* false-positive: re-checked afterwards */
    if (!is_dir('...') && !mkdir('...') && !is_dir('...')) {}
    if (is_dir('...') || mkdir('...') || is_dir('...')) {}
}