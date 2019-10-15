<?php

    $weak         = <error descr="[EA] A weak hash generated, consider providing '$2y$<cost and salt>' (Blowfish) as the second argument.">crypt('password')</error>;
    $insecure     = <error descr="[EA] '$2y$<cost and salt>' should be used in preference to insecure '$2a$<cost and salt>'.">crypt</error> ('password', '$2a$...');
    $passwordHash = <weak_warning descr="[EA] Use of password_hash(..., PASSWORD_BCRYPT) is encouraged in this case (uses $2y$ with cost of 10).">crypt</weak_warning> ('password', '$2y$...');

    function wrapCrypt($rounds, $salt) {
        $type = '2a';
        <error descr="[EA] '$2y$<cost and salt>' should be used in preference to insecure '$2a$<cost and salt>'.">crypt</error> ('password', '$' . $type . '$' . $rounds . '$' . $salt);
    }