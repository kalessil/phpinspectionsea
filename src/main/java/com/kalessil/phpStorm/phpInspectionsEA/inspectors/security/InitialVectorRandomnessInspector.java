package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

/**
 - string openssl_encrypt ( string $data , string $method , string $password [, int $options = 0 [, string $iv = "" ]] )
 - string mcrypt_encrypt ( string $cipher , string $key , string $data , string $mode [, string $iv ] )
    - only check if IV was provided
    - ensure that IV is not a constant (initial vector is not random)
    - ensure iv is from random_bytes|openssl_random_pseudo_bytes|mcrypt_create_iv
 */
public class InitialVectorRandomnessInspector {
}
