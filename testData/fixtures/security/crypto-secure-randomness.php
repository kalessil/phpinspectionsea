<?php

class CryptoSecureRandomness
{
    public function missingSecondParameters() {
        $x = <error descr="[EA] Use 2nd parameter for determining if the algorithm used was cryptographically strong.">openssl_random_pseudo_bytes</error> (32);
        if ($x === false) {
            return false;
        }

        $x = <error descr="[EA] Please provide 2nd parameter implicitly as default value has changed between PHP versions.">mcrypt_create_iv</error> (32);
        if ($x === false) {
            return false;
        }

        return $x;
    }

    public function hardenSecondParameters() {
        $x = openssl_random_pseudo_bytes (32, <error descr="[EA] $crypto_strong can be false, please add necessary checks.">$isSecure</error>);
        if ($x === false) {
            return false;
        }

        $x = mcrypt_create_iv (32, <error descr="[EA] It's better to use MCRYPT_DEV_RANDOM here (may block until more entropy is available).">MCRYPT_DEV_URANDOM</error>);
        if ($x === false) {
            return false;
        }

        return $x;
    }

    public function missingResultsVerification()
    {
        $x = <error descr="[EA] The IV generated can be false, please add necessary checks.">openssl_random_pseudo_bytes</error> (32, $isCryptoStrong);
        $x = @<error descr="[EA] The IV generated can be false, please add necessary checks.">openssl_random_pseudo_bytes</error> (32, $isCryptoStrong);
        if (false === $isCryptoStrong) {
            return false;
        }

        $x = <error descr="[EA] The IV generated can be false, please add necessary checks.">mcrypt_create_iv</error> (32, MCRYPT_DEV_RANDOM);
        $x = @<error descr="[EA] The IV generated can be false, please add necessary checks.">mcrypt_create_iv</error> (32, MCRYPT_DEV_RANDOM);

        return $x;
    }

    public function indirectFalseCheck() {
        $x = openssl_random_pseudo_bytes (32, $isSecure);
        if (!$x || !$isSecure) {
            return false;
        }

        $x = mcrypt_create_iv (32, <error descr="[EA] It's better to use MCRYPT_DEV_RANDOM here (may block until more entropy is available).">MCRYPT_DEV_URANDOM</error>);
        if (!$x) {
            return false;
        }

        return $x;
    }
}