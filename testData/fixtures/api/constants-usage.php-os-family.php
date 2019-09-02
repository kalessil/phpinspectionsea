<?php

function cases_holder() {
    return [
        <weak_warning descr="Consider using 'PHP_OS_FAMILY' instead.">strpos(PHP_OS, 'WIN')</weak_warning> === 0,
        <weak_warning descr="Consider using 'PHP_OS_FAMILY' instead.">stripos(PHP_OS, 'WIN')</weak_warning> === 0,
        <weak_warning descr="Consider using 'PHP_OS_FAMILY' instead.">mb_strpos(PHP_OS, 'WIN')</weak_warning> === 0,
        <weak_warning descr="Consider using 'PHP_OS_FAMILY' instead.">mb_stripos(PHP_OS, 'WIN')</weak_warning> === 0,
        <weak_warning descr="Consider using 'PHP_OS_FAMILY' instead.">strpos(PHP_OS, 'WIN')</weak_warning> !== false,

        <weak_warning descr="Consider using 'PHP_OS_FAMILY' instead.">strncmp('win', PHP_OS, 3)</weak_warning> === 0,
        <weak_warning descr="Consider using 'PHP_OS_FAMILY' instead.">strncasecmp('win', PHP_OS, 3)</weak_warning> !== 0,

        <weak_warning descr="Consider using 'PHP_OS_FAMILY' instead.">strtolower(substr(PHP_OS, 0, 3))</weak_warning> === 'win',
        <weak_warning descr="Consider using 'PHP_OS_FAMILY' instead.">strtoupper(substr(PHP_OS, 0, 3))</weak_warning> !== 'WIN',
    ];
}