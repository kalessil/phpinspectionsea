<?php

function test_cases() {
    return [
        <warning descr="[EA] 'parse_url('uri', PHP_URL_SCHEME)' could be used here (directly extracts the desired part).">parse_url('uri')['scheme']</warning>,
        <warning descr="[EA] 'pathinfo('uri', PATHINFO_DIRNAME)' could be used here (directly extracts the desired part).">pathinfo('uri')['dirname']</warning>,
    ];
}