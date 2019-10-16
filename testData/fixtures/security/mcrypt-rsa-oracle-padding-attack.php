<?php

// https://paragonie.com/blog/2015/05/if-you-re-typing-word-mcrypt-into-your-code-you-re-doing-it-wrong
class Clazz
{
    private $mode = MCRYPT_MODE_CBC;
    const MODE    = MCRYPT_MODE_CBC;

    public function casesHolder($optionalParameter = MCRYPT_MODE_CBC)
    {
        $localVariable = MCRYPT_MODE_CBC;

        <error descr="[EA] This call is vulnerable to oracle padding attacks (see our documentation on GitHub for options).">mcrypt_encrypt(MCRYPT_RIJNDAEL_128, '', '', MCRYPT_MODE_CBC)</error>;
        <error descr="[EA] This call is vulnerable to oracle padding attacks (see our documentation on GitHub for options).">mcrypt_encrypt(MCRYPT_RIJNDAEL_128, '', '', self::MODE)</error>;
        <error descr="[EA] This call is vulnerable to oracle padding attacks (see our documentation on GitHub for options).">mcrypt_encrypt(MCRYPT_RIJNDAEL_128, '', '', $this->mode)</error>;
        <error descr="[EA] This call is vulnerable to oracle padding attacks (see our documentation on GitHub for options).">mcrypt_encrypt(MCRYPT_RIJNDAEL_128, '', '', $optionalParameter)</error>;
        <error descr="[EA] This call is vulnerable to oracle padding attacks (see our documentation on GitHub for options).">mcrypt_encrypt(MCRYPT_RIJNDAEL_128, '', '', $localVariable)</error>;

        mcrypt_encrypt(MCRYPT_3DES, '', '', MCRYPT_MODE_CBC);
    }
}