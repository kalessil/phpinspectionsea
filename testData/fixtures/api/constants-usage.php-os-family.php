<?php

function cases_holder() {
    return [
        strpos(PHP_OS, 'WIN') === 0,
        stripos(PHP_OS, 'WIN') === 0,
        mb_strpos(PHP_OS, 'WIN') === 0,
        mb_stripos(PHP_OS, 'WIN') === 0,
        strpos(PHP_OS, 'WIN') !== false,

        strncmp('win', PHP_OS, 3) === 0,
        strncasecmp('win', PHP_OS, 3) !== 0,

        strtolower(substr(PHP_OS, 0, 3)) === 'win',
        strtoupper(substr(PHP_OS, 0, 3)) !== 'WIN',
    ];
}