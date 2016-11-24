<?php

class IvRandomness {
    private $iv = 'default';
    private $_iv;

    private function notHandledCases() {
        openssl_encrypt('data', 'method', 'password');
        mcrypt_encrypt('cipher', 'key', 'data', 'mode');
    }

    private function validCases($iv) {
        openssl_encrypt('data', 'method', 'password', 0, random_bytes(32));
        mcrypt_encrypt('cipher', 'key', 'data', 'mode', random_bytes(32));

        $iv = random_bytes(32);
        $iv = openssl_random_pseudo_bytes(32);
        $iv = mcrypt_create_iv(32);

        openssl_encrypt('data', 'method', 'password', 0, $iv);
        mcrypt_encrypt('cipher', 'key', 'data', 'mode', $iv);

        $this->_iv = random_bytes(32);
        $this->_iv = openssl_random_pseudo_bytes(32);
        $this->_iv = mcrypt_create_iv(32);

        openssl_encrypt('data', 'method', 'password', 0, $this->_iv);
        mcrypt_encrypt('cipher', 'key', 'data', 'mode', $this->_iv);
    }

    private function invalidCases($iv){
        $iv = 'variable';
        $iv = mt_rand();

        openssl_encrypt('data', 'method', 'password', 0, $iv);       // <- reported, report parameter?
        mcrypt_encrypt('cipher', 'key', 'data', 'mode', $iv);        // <- reported, report parameter?

        $this->iv = 'property';
        $this->iv = mt_rand();

        openssl_encrypt('data', 'method', 'password', 0, $this->iv); // <- reported, report parameter?
        mcrypt_encrypt('cipher', 'key', 'data', 'mode', $this->iv);  // <- reported, report parameter?

    }
}