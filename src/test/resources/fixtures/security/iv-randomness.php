<?php

class IvRandomness {
    private $iv = 'default';

    private function notHandledCases() {
        openssl_encrypt('data', 'method', 'password');
        mcrypt_encrypt('cipher', 'key', 'data', 'mode');
    }

    private function validCases($iv) {
        $iv = random_bytes(32);
        $iv = openssl_random_pseudo_bytes(32);
        $iv = mcrypt_create_iv(32);
        $iv = 'variable';
        $iv = mt_rand();

        $this->iv = random_bytes(32);
        $this->iv = openssl_random_pseudo_bytes(32);
        $this->iv = mcrypt_create_iv(32);
        $this->iv = 'property';
        $this->iv = mt_rand();

        openssl_encrypt('data', 'method', 'password', 0, $iv);
        mcrypt_encrypt('cipher', 'key', 'data', 'mode', $iv);

        openssl_encrypt('data', 'method', 'password', 0, $this->iv);
        mcrypt_encrypt('cipher', 'key', 'data', 'mode', $this->iv);

        openssl_encrypt('data', 'method', 'password', 0, random_bytes(32));
        mcrypt_encrypt('cipher', 'key', 'data', 'mode', random_bytes(32));
    }
}