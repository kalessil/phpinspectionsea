<?php

// https://paragonie.com/blog/2015/05/if-you-re-typing-word-mcrypt-into-your-code-you-re-doing-it-wrong
class McryptlRsaPaddingOracle
{
    private $mode = MCRYPT_MODE_CBC;
    const MODE    = MCRYPT_MODE_CBC;

    public function pattern1($optionalParameter = MCRYPT_MODE_CBC)
    {
        $encrypted     = '';
        $key           = '';
        $localVariable = MCRYPT_MODE_CBC;

        <error descr="This call is vulnerable to oracle padding attacks (see our documentation on GitHub for options).">mcrypt_encrypt(MCRYPT_RIJNDAEL_128, $key, $encrypted, MCRYPT_MODE_CBC)</error>;
        <error descr="This call is vulnerable to oracle padding attacks (see our documentation on GitHub for options).">mcrypt_encrypt(MCRYPT_RIJNDAEL_128, $key, $encrypted, self::MODE)</error>;
        <error descr="This call is vulnerable to oracle padding attacks (see our documentation on GitHub for options).">mcrypt_encrypt(MCRYPT_RIJNDAEL_128, $key, $encrypted, $this->mode)</error>;
        <error descr="This call is vulnerable to oracle padding attacks (see our documentation on GitHub for options).">mcrypt_encrypt(MCRYPT_RIJNDAEL_128, $key, $encrypted, $optionalParameter)</error>;
        <error descr="This call is vulnerable to oracle padding attacks (see our documentation on GitHub for options).">mcrypt_encrypt(MCRYPT_RIJNDAEL_128, $key, $encrypted, $localVariable)</error>;
    }

    public function pattern2($optionalParameter = MCRYPT_MODE_CBC)
    {
        $encrypted     = '';
        $key           = '';
        $localVariable = MCRYPT_MODE_CBC;

        mcrypt_encrypt(MCRYPT_3DES, $key, $encrypted, MCRYPT_MODE_CBC);
        mcrypt_encrypt(MCRYPT_3DES, $key, $encrypted, self::MODE);
        mcrypt_encrypt(MCRYPT_3DES, $key, $encrypted, $this->mode);
        mcrypt_encrypt(MCRYPT_3DES, $key, $encrypted, $optionalParameter);
        mcrypt_encrypt(MCRYPT_3DES, $key, $encrypted, $localVariable);
    }
}