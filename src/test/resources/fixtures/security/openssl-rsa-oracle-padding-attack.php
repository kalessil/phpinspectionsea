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
        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">$function1('', $encrypted, '')</error>;

        $function2 = 'openssl_public_encrypt';
        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">$function2('', $encrypted, '')</error>;

        return $encrypted;
    }

    public function pattern2($optionalParameter = OPENSSL_PKCS1_PADDING)
    {
        $encrypted     = '';
        $localVariable = OPENSSL_PKCS1_PADDING;

        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">openssl_public_encrypt('', $encrypted, '')</error>;
        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">openssl_public_encrypt('', $encrypted, '', OPENSSL_PKCS1_PADDING)</error>;
        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">openssl_public_encrypt('', $encrypted, '', self::PADDING)</error>;
        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">openssl_public_encrypt('', $encrypted, '', $this->padding)</error>;
        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">openssl_public_encrypt('', $encrypted, '', $optionalParameter)</error>;
        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">openssl_public_encrypt('', $encrypted, '', $localVariable)</error>;

        openssl_private_encrypt('', $encrypted, '');
        openssl_private_encrypt('', $encrypted, '', OPENSSL_PKCS1_PADDING);
        openssl_private_encrypt('', $encrypted, '', self::PADDING);
        openssl_private_encrypt('', $encrypted, '', $optionalParameter);
        openssl_private_encrypt('', $encrypted, '', $localVariable);
    }

    public function pattern3($optionalParameter = OPENSSL_PKCS1_PADDING)
    {
        $encrypted     = '';
        $localVariable = OPENSSL_PKCS1_PADDING;

        openssl_public_decrypt('', $encrypted, '');
        openssl_public_decrypt('', $encrypted, '', OPENSSL_PKCS1_PADDING);
        openssl_public_decrypt('', $encrypted, '', self::PADDING);
        openssl_public_decrypt('', $encrypted, '', $this->padding);
        openssl_public_decrypt('', $encrypted, '', $optionalParameter);
        openssl_public_decrypt('', $encrypted, '', $localVariable);

        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">openssl_private_decrypt('', $encrypted, '')</error>;
        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">openssl_private_decrypt('', $encrypted, '', OPENSSL_PKCS1_PADDING)</error>;
        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">openssl_private_decrypt('', $encrypted, '', self::PADDING)</error>;
        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">openssl_private_decrypt('', $encrypted, '', $this->padding)</error>;
        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">openssl_private_decrypt('', $encrypted, '', $optionalParameter)</error>;
        <error descr="This call is vulnerable to oracle padding attacks, use OPENSSL_PKCS1_OAEP_PADDING as 4th argument.">openssl_private_decrypt('', $encrypted, '', $localVariable)</error>;
    }
}