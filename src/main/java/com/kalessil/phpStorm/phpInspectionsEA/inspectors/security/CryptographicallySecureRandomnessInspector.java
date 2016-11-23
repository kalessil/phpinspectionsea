package com.kalessil.phpStorm.phpInspectionsEA.inspectors.security;

/**
 - Cryptographically secure random:
     - openssl_random_pseudo_bytes()
         - use random_bytes() PHP7
         - report missing 2nd argument;
         - report if argument is not verified;
     - mcrypt_create_iv() provide
         - use random_bytes() PHP7
         - report if MCRYPT_DEV_RANDOM (strong) MCRYPT_DEV_RANDOM (secure) not provided as 2nd argument
         - report using MCRYPT_DEV_RANDOM and side-effects;
 */

public class CryptographicallySecureRandomnessInspector {
}
