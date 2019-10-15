<?php

function cases_holder_file() {
    $glue = '';
    $content = <warning descr="[EA] Consider using 'file_get_contents('...')' instead (consumes less cpu and memory resources).">implode($glue, file('...'))</warning>;
    $content = <warning descr="[EA] Consider using 'file_get_contents('...')' instead (consumes less cpu and memory resources).">implode('', file('...'))</warning>;

    $content = implode(file('...')); // should be not reported, but add extra complexity into implementation
    $content = implode('-', file('...'));
    $content = implode('', file('...', FILE_IGNORE_NEW_LINES));
    $content = implode('', file('...', FILE_USE_INCLUDE_PATH, null));
}

function cases_holder_http_build_query() {
    $content = <warning descr="[EA] Consider taking advantage of using 'http_build_query(...)' here (simplification).">implode('&', [])</warning>;
    $content = <warning descr="[EA] Consider taking advantage of using 'http_build_query(...)' here (simplification).">implode('&amp;', [])</warning>;
    $content = implode('&&', []);
}

function cases_holder_explode() {
    $content = <warning descr="[EA] Consider using 'str_replace(',', '...', [])' instead (consumes less cpu and memory resources).">implode('...', explode(',', []))</warning>;
    $content = implode('...', explode(',', [], 1));
}

function cases_holder_sprintf() {
    $content = sprintf('%s', <warning descr="[EA] Consider taking advantage of the outer 'sprintf(...)' call instead (simplification).">implode(',', [])</warning>);
    $content = sprintf('%s', implode(',', $array));
}

function cases_holder_few_arguments() {
    $content = <warning descr="[EA] Consider using '$singleElement' instead (consumes less cpu and memory resources).">implode('', [$singleElement])</warning>;
    $content = implode('', ['...', '...']);
}
