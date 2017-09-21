<?php

function cases_holder() {
    /* case: just a call -> conditionally throw exception */
    if (!mkdir('...') && !is_dir('...')) {
        throw new \RuntimeException(sprintf('Directory "%s" was not created', '...'));
    }
    if (!mkdir('...') && !is_dir('...')) {
        throw new \RuntimeException(sprintf('Directory "%s" was not created', '...'));
    }
    if (!is_dir('...')) {
        if (!mkdir('...') && !is_dir('...')) {
            throw new \RuntimeException(sprintf('Directory "%s" was not created', '...'));
        }
    }

    /* false-positive: result saved */
    $result = mkdir('...');

    /* case: incomplete conditions */
    if (!mkdir('...') && !is_dir('...')) {}
    if (!mkdir('...') && !is_dir('...')) {}
    if (!mkdir('...') && !is_dir('...')) {}
    if (!is_dir('...') && !mkdir('...') && !is_dir('...')) {}
    if (is_dir('...') || mkdir('...') || is_dir('...')) {}

    /* false-positive: re-checked afterwards */
    if (!is_dir('...') && !mkdir('...') && !is_dir('...')) {}
    if (is_dir('...') || mkdir('...') || is_dir('...')) {}
}