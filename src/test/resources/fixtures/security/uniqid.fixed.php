<?php

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