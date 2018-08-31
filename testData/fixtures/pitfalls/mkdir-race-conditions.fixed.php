<?php

function cases_holder() {
    /* case: just a call -> conditionally throw exception */
    if (!mkdir('...', 0644) && !is_dir('...')) {
        throw new \RuntimeException(sprintf('Directory "%s" was not created', '...'));
    }
    if (!mkdir('...', 0644) && !is_dir('...')) {
        throw new \RuntimeException(sprintf('Directory "%s" was not created', '...'));
    }
    if (!is_dir('...')) {
        if (!mkdir('...', 0644) && !is_dir('...')) {
            throw new \RuntimeException(sprintf('Directory "%s" was not created', '...'));
        }
    }

    /* false-positive: result saved */
    $result = mkdir('...');

    /* case: incomplete conditions */
    if (!mkdir('...', 0644) && !is_dir('...')) {}
    if (!mkdir('...', 0644) && !is_dir('...')) {}
    if (!mkdir('...', 0644) && !is_dir('...')) {}
    if (!is_dir('...') && !mkdir('...', 0644) && !is_dir('...')) {}
    if (is_dir('...') || mkdir('...', 0644) || is_dir('...')) {}

    /* false-positive: re-checked afterwards */
    if (!is_dir('...') && !mkdir('...') && !is_dir('...')) {}
    if (is_dir('...') || mkdir('...') || is_dir('...')) {}
}

function quickfix_with_variable() {
    /* case: just a call -> conditionally throw exception */
    if (!mkdir($concurrentDirectory = trim('...')) && !is_dir($concurrentDirectory)) {
        throw new \RuntimeException(sprintf('Directory "%s" was not created', $concurrentDirectory));
    }

    /* case: incomplete conditions */
    if (!mkdir($concurrentDirectory = trim('...')) && !is_dir($concurrentDirectory)) {}
    if (!mkdir($concurrentDirectory = trim('...')) && !is_dir($concurrentDirectory)) {}
    if (!is_dir(trim('...')) && !mkdir($concurrentDirectory = trim('...')) && !is_dir($concurrentDirectory)) {}
    if (is_dir(trim('...')) || mkdir($concurrentDirectory = trim('...')) || is_dir($concurrentDirectory)) {}
}