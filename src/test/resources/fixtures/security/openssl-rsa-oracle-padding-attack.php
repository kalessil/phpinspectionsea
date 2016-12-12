<?php

// https://paragonie.com/blog/2016/12/everything-you-know-about-public-key-encryption-in-php-is-wrong
class OpensslRsaPaddingOracle
{
    private $padding = OPENSSL_PKCS1_PADDING;
    const PADDING    = OPENSSL_PKCS1_PADDING;

    public function pattern1()
    {
        $encrypted = '';

        $function1 = true ? 'openssl_public_encrypt' : 'openssl_private_encrypt';
        $function1('', $encrypted, '');          // <- reported

        $function2 = 'openssl_public_encrypt';
        $function2('', $encrypted, '');          // <- reported

        $function3 = 'openssl_private_encrypt';
        $function3('', $encrypted, '');          // <- reported

        return $encrypted;
    }

    public function pattern2($optionalParameter = OPENSSL_PKCS1_PADDING)
    {
        $encrypted     = '';
        $localVariable = OPENSSL_PKCS1_PADDING;

        openssl_public_encrypt('', $encrypted, '');                         // <- reported
        openssl_public_encrypt('', $encrypted, '', OPENSSL_PKCS1_PADDING);  // <- reported
        openssl_public_encrypt('', $encrypted, '', self::PADDING);          // <- reported
        openssl_public_encrypt('', $encrypted, '', $this->padding);         // <- reported
        openssl_public_encrypt('', $encrypted, '', $optionalParameter);     // <- reported
        openssl_public_encrypt('', $encrypted, '', $localVariable);         // <- reported

        openssl_private_encrypt('', $encrypted, '');                        // <- reported
        openssl_private_encrypt('', $encrypted, '', OPENSSL_PKCS1_PADDING); // <- reported
        openssl_private_encrypt('', $encrypted, '', self::PADDING);         // <- reported
        openssl_private_encrypt('', $encrypted, '', $optionalParameter);    // <- reported
        openssl_private_encrypt('', $encrypted, '', $localVariable);        // <- reported
    }

    public function pattern3($optionalParameter = OPENSSL_PKCS1_PADDING)
    {
        $encrypted     = '';
        $localVariable = OPENSSL_PKCS1_PADDING;

        openssl_public_decrypt('', $encrypted, '');                         // <- reported
        openssl_public_decrypt('', $encrypted, '', OPENSSL_PKCS1_PADDING);  // <- reported
        openssl_public_decrypt('', $encrypted, '', self::PADDING);          // <- reported
        openssl_public_decrypt('', $encrypted, '', $this->padding);         // <- reported
        openssl_public_decrypt('', $encrypted, '', $optionalParameter);     // <- reported
        openssl_public_decrypt('', $encrypted, '', $localVariable);         // <- reported

        openssl_private_decrypt('', $encrypted, '');                        // <- reported
        openssl_private_decrypt('', $encrypted, '', OPENSSL_PKCS1_PADDING); // <- reported
        openssl_private_decrypt('', $encrypted, '', self::PADDING);         // <- reported
        openssl_private_decrypt('', $encrypted, '', $this->padding);        // <- reported
        openssl_private_decrypt('', $encrypted, '', $optionalParameter);    // <- reported
        openssl_private_decrypt('', $encrypted, '', $localVariable);        // <- reported
    }
}