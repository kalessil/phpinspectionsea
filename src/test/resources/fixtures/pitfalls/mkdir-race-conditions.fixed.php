<?php

    if (!mkdir('...') && !is_dir('...')) {
        throw new RuntimeException(sprintf('Directory "%s" was not created', '...'));
    }

    /* false-positive: result saved */
    $result = mkdir('...');


    if (!is_dir('...')) {
        if (!mkdir('...') && !is_dir('...')) {
            throw new RuntimeException(sprintf('Directory "%s" was not created', '...'));
        }
    }

    if (!mkdir('...') && !is_dir('...')) {}


    if (!is_dir('...') && !mkdir('...') && !is_dir('...')) {}

    /* false-positive: re-checked afterwards */
    if (!is_dir('...') && !mkdir('...') && !is_dir('...')) {}