<?php

class CryptoSecureRandomnessPhp7
{
    public function encrypt()
    {
        $x = <weak_warning descr="Consider using cryptographically secure random_bytes() instead.">openssl_random_pseudo_bytes</weak_warning> (32, $isSecure);
        if ($isSecure === false || $x === false) {
            return;
        }

        $x = <weak_warning descr="Consider using cryptographically secure random_bytes() instead.">mcrypt_create_iv</weak_warning> (32, MCRYPT_DEV_RANDOM);
        if ($x === false) {
            return;
        }
    }
}