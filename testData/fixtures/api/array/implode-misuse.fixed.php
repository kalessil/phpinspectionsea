<?php

function cases_holder_file() {
    $glue = '';
    $content = file_get_contents('...');
    $content = file_get_contents('...');

    $content = implode(file('...')); // should be not reported, but add extra complexity into implementation
    $content = implode('-', file('...'));
    $content = implode('', file('...', FILE_IGNORE_NEW_LINES));
    $content = implode('', file('...', FILE_USE_INCLUDE_PATH, null));
}

function cases_holder_explode() {
    $content = str_replace(',', '...', []);
    $content = implode('...', explode(',', [], 1));
}

function cases_holder_sprintf() {
    $content = sprintf('%s', implode(',', []));
    $content = sprintf('%s', implode(',', $array));
}

function cases_holder_few_arguments() {
    $content = implode('', [$singleElement]);
    $content = implode('', ['...', '...']);
}
