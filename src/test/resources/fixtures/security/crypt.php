<?php

    $weak         = <error descr="A weak hash generated, consider providing '$2y$' (Blowfish) as the second argument.">crypt</error> ('password');
    $insecure     = <error descr="'$2y$' should be used in preference to insecure '$2a$'.">crypt</error> ('password', '$2a$');
    $passwordHash = <error descr="Use of password_hash(..., PASSWORD_BCRYPT) is encouraged in this case (uses $2y$ salt).">crypt</error> ('password', '$2y$');