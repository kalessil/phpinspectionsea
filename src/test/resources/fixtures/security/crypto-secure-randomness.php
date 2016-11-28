<?php

class CryptoSecureRandomness
{
    public function missingSecondParameters() {
        $x = <error descr="Use 2nd parameter for determining if the algorithm used was cryptographically strong">openssl_random_pseudo_bytes</error> (32);
        if ($x === false) {
            return;
        }

        $x = <error descr="Please provide 2nd parameter implicitly as default value has changed between PHP versions">mcrypt_create_iv</error> (32);
        if ($x === false) {
            return;
        }
    }
}