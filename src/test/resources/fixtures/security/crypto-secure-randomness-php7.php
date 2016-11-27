<?php

    $x = openssl_random_pseudo_bytes(32, $isSecure);
    if ($isSecure === false || $x === false) {
        return;
    }

    $x = mcrypt_create_iv(32, MCRYPT_DEV_RANDOM);
    if ($x === false) {
        return;
    }