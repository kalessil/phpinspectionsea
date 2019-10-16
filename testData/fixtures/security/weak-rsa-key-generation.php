<?php

    <error descr="[EA] The generated RSA key length is insufficient (system defaults are 1024 or 2048 bits), using 4096 bits is recommended.">openssl_pkey_new()</error>;
    <error descr="[EA] The generated RSA key length is insufficient (system defaults are 1024 or 2048 bits), using 4096 bits is recommended.">openssl_pkey_new([])</error>;
    <error descr="[EA] The generated RSA key length is insufficient (system defaults are 1024 or 2048 bits), using 4096 bits is recommended.">openssl_pkey_new(['private_key_type' => OPENSSL_KEYTYPE_RSA])</error>;
    <error descr="[EA] The generated RSA key length is insufficient, using 4096 bits is recommended.">openssl_pkey_new(['private_key_bits' => 1024])</error>;
    <error descr="[EA] The generated RSA key length is insufficient, using 4096 bits is recommended.">openssl_pkey_new(['private_key_bits' => 1024, 'private_key_type' => OPENSSL_KEYTYPE_RSA])</error>;

    openssl_pkey_new(['private_key_bits' => 4096, 'private_key_type' => OPENSSL_KEYTYPE_RSA]);

    <error descr="[EA] The generated RSA key length is insufficient (system defaults are 1024 or 2048 bits), using 4096 bits is recommended.">openssl_csr_new($dn, $key)</error>;
    <error descr="[EA] The generated RSA key length is insufficient (system defaults are 1024 or 2048 bits), using 4096 bits is recommended.">openssl_csr_new($dn, $key, [])</error>;
    <error descr="[EA] The generated RSA key length is insufficient (system defaults are 1024 or 2048 bits), using 4096 bits is recommended.">openssl_csr_new($dn, $key, ['private_key_type' => OPENSSL_KEYTYPE_RSA])</error>;
    <error descr="[EA] The generated RSA key length is insufficient, using 4096 bits is recommended.">openssl_csr_new($dn, $key, ['private_key_bits' => 1024])</error>;
    <error descr="[EA] The generated RSA key length is insufficient, using 4096 bits is recommended.">openssl_csr_new($dn, $key, ['private_key_bits' => 1024, 'private_key_type' => OPENSSL_KEYTYPE_RSA])</error>;

    openssl_csr_new($dn, $key, ['private_key_bits' => 4096, 'private_key_type' => OPENSSL_KEYTYPE_RSA]);
