<?php

class OraclePaddingAttacks
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

    public function pattern2()
    {
        $encrypted = '';

        openssl_public_encrypt('', $encrypted, '');                         // <- reported
        openssl_public_encrypt('', $encrypted, '', OPENSSL_PKCS1_PADDING);  // <- reported
        openssl_public_encrypt('', $encrypted, '', self::PADDING);          // <- reported
        openssl_public_encrypt('', $encrypted, '', $this->padding);         // <- reported

        openssl_private_encrypt('', $encrypted, '');                        // <- reported
        openssl_private_encrypt('', $encrypted, '', OPENSSL_PKCS1_PADDING); // <- reported
        openssl_private_encrypt('', $encrypted, '', self::PADDING);         // <- reported
        openssl_private_encrypt('', $encrypted, '', $this->padding);        // <- reported
    }

    public function pattern3()
    {
        $encrypted = '';

        openssl_public_decrypt('', $encrypted, '');                         // <- reported
        openssl_public_decrypt('', $encrypted, '', OPENSSL_PKCS1_PADDING);  // <- reported
        openssl_public_decrypt('', $encrypted, '', self::PADDING);          // <- reported
        openssl_public_decrypt('', $encrypted, '', $this->padding);         // <- reported

        openssl_private_decrypt('', $encrypted, '');                        // <- reported
        openssl_private_decrypt('', $encrypted, '', OPENSSL_PKCS1_PADDING); // <- reported
        openssl_private_decrypt('', $encrypted, '', self::PADDING);         // <- reported
        openssl_private_decrypt('', $encrypted, '', $this->padding);        // <- reported
    }
}