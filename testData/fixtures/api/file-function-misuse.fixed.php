<?php

function cases_holder() {
    $content = file_get_contents('...');
    $content = @file_get_contents('...');
    $content = file_get_contents('...');

    $a = '';
    $content = file_get_contents('...');

    $content = file_get_contents('...');

    $content = implode('-', file('...'));
    $content = implode('', file('...', FILE_IGNORE_NEW_LINES));
    $content = implode('', file('...', FILE_USE_INCLUDE_PATH, null));
}