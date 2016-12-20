<?php

    $weak         = <error descr="A weak hash generated, consider providing '$2y$<cost and salt>' (Blowfish) as the second argument.">crypt</error> ('password');
    $insecure     = <error descr="'$2y$<cost and salt>' should be used in preference to insecure '$2a$<cost and salt>'.">crypt</error> ('password', '$2a$...');
    $passwordHash = <error descr="Use of password_hash(..., PASSWORD_BCRYPT) is encouraged in this case (uses $2y$ with cost of 10).">crypt</error> ('password', '$2y$...');