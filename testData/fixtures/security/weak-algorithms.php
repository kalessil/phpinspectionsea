<?php

class SingleCaseTest
{
    private function method()
    {
        /* false-positive: test cases */
        echo MCRYPT_RIJNDAEL_192;
    }
}

function casesHolder()
{
    echo <error descr="mcrypt's MCRYPT_RIJNDAEL_192 is not AES compliant, MCRYPT_RIJNDAEL_128 should be used instead.">MCRYPT_RIJNDAEL_192</error>;
    echo <error descr="mcrypt's MCRYPT_RIJNDAEL_256 is not AES compliant, MCRYPT_RIJNDAEL_128 + 256-bit key should be used instead.">MCRYPT_RIJNDAEL_256</error>;
    echo <error descr="3DES has known vulnerabilities, consider using MCRYPT_RIJNDAEL_128 instead.">MCRYPT_3DES</error>;
    echo <error descr="DES has known vulnerabilities, consider using MCRYPT_RIJNDAEL_128 instead.">MCRYPT_DES</error>;
    echo <error descr="RC2 has known vulnerabilities, consider using MCRYPT_RIJNDAEL_128 instead.">MCRYPT_RC2</error>;
    echo <error descr="RC4 has known vulnerabilities, consider using MCRYPT_RIJNDAEL_128 instead.">MCRYPT_RC4</error>;
    echo <error descr="RC4 has known vulnerabilities, consider using MCRYPT_RIJNDAEL_128 instead.">MCRYPT_ARCFOUR</error>;
    echo <error descr="3DES has known vulnerabilities, consider using AES-128-* instead.">OPENSSL_CIPHER_3DES</error>;
    echo <error descr="DES has known vulnerabilities, consider using AES-128-* instead.">OPENSSL_CIPHER_DES</error>;
    echo <error descr="RC2 has known vulnerabilities, consider using AES-128-* instead.">OPENSSL_CIPHER_RC2_40</error>;
    echo <error descr="RC2 has known vulnerabilities, consider using AES-128-* instead.">OPENSSL_CIPHER_RC2_64</error>;
    echo <error descr="MD5 has known vulnerabilities, consider using CRYPT_BLOWFISH instead.">CRYPT_MD5</error>;
    echo <error descr="DES has known vulnerabilities, consider using CRYPT_BLOWFISH instead.">CRYPT_STD_DES</error>;
}
