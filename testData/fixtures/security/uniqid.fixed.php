<?php

namespace {
    use function uniqid;

    uniqid('', true);
    uniqid('q', true);

    call_user_func(function ($value) {
        return uniqid($value, true);
    }, '');
    call_user_func(function ($value) {
        return uniqid($value, true);
    }, '');

    call_user_func('uniqid');
    uniqid('', true);
    uniqid(more_entropy: true);
}