<?php

class IvRandomness {
    private $iv = 'default';
    private $_iv;
    const IV = 'constant';

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

    private function invalidCases($iv = 'variable') {
        $iv       = mt_rand();
        $this->iv = mt_rand();

        openssl_encrypt('data', 'method', 'password', 0,
            <error descr="[EA] openssl_random_pseudo_bytes() should be used for IV, but found: 'variable', mt_rand().">$iv</error>);
        mcrypt_encrypt('cipher', 'key', 'data', 'mode',
            <error descr="[EA] mcrypt_create_iv() should be used for IV, but found: 'variable', mt_rand().">$iv</error>);

        openssl_encrypt('data', 'method', 'password', 0,
            <error descr="[EA] openssl_random_pseudo_bytes() should be used for IV, but found: 'default', mt_rand().">$this->iv</error>);
        mcrypt_encrypt('cipher', 'key', 'data', 'mode',
            <error descr="[EA] mcrypt_create_iv() should be used for IV, but found: 'default', mt_rand().">$this->iv</error>);

        openssl_encrypt('data', 'method', 'password', 0,
            <error descr="[EA] openssl_random_pseudo_bytes() should be used for IV, but found: 'constant'.">self::IV</error>);
        mcrypt_encrypt('cipher', 'key', 'data', 'mode',
            <error descr="[EA] mcrypt_create_iv() should be used for IV, but found: 'constant'.">self::IV</error>);
    }

    private function wrappedRandomBytes($length) {
        return random_bytes($length);
    }

    private function testWrappedRandomBytes() {
        openssl_encrypt('...', '...', '...', 0, $this->wrappedRandomBytes(32));
    }
}