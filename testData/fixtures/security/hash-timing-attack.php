<?php

function cases_holder() {
    return [
        <error descr="This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.">md5('...') == '...'</error>,
        <error descr="This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.">md5('...') != '...'</error>,
        <error descr="This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.">md5('...') === '...'</error>,
        <error descr="This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.">md5('...') !== '...'</error>,
        <error descr="This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.">strncmp(md5('...'), '...', 32)</error>,
        <error descr="This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.">strcmp(md5('...'), '...')</error>,
    ];
}

function cases_holder_workflow() {
    $local1 = md5('...');
    $local2 = md5('...');
    $local3 = md5('...');
    $local4 = md5('...');
    $local5 = md5('...');
    $local6 = md5('...');

    return [
        <error descr="This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.">$local1 == '...'</error>,
        <error descr="This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.">$local2 != '...'</error>,
        <error descr="This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.">$local3 === '...'</error>,
        <error descr="This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.">$local4 !== '...'</error>,
        <error descr="This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.">strncmp($local5, '...', 32)</error>,
        <error descr="This construct is probably vulnerable to hash timing attacks, please use 'hash_equals(...)' or 'password_verify(...)' instead.">strcmp($local6, '...')</error>,
    ];
}