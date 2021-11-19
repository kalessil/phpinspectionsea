# Security

## Exploiting unserialize

> When Php Inspection (EA Extended) reports the rule violations, ensure that it does not belong to any dev-tools 
> (profilers, dev-environment specific caching etc). Dev-tools should not go to production and normally 
> do not have any security guarantee.

The vulnerability allows remote code execution or code/SQL injection by using 
the unserialize() function (more details here: https://www.owasp.org/index.php/PHP_Object_Injection).

There are several options to resolve the issue:

### Using JSON format

This approach is straightforward: use json_decode()/json_encode() instead of serialization.
Unfortunately it is not suitable for performance-optimized components.

### PHP 7.0.0+

Since PHP 7.0.0 the unserialize function has a second parameter $options, which allows 
implicitly specifying which classes can be unserialized.

```php
    /* graceful approach which supports older versions of PHP */ 
    if (PHP_VERSION_ID >= 70000) {
        /* to forbid classes unserializing at all use this: array('allowed_classes' => false) */
        $unserializedData = unserialize($serializedData, ['allowed_classes' => ['Class1', 'Class2']]);
    } else {
        $unserializedData = unserialize($serializedData);
    }
```

### Hooking into unserialize callback

This technique hooks into class-loading during unserialize, making it possible to prevent new classes from loading.

```php
    function __autoload($classname) {
        /* leave it empty to prevent further autoloading, only loaded classes will be available */
    }
    
    $originalCallback = ini_set('unserialize_callback_func', '__autoload');
    $unserializedData = unserialize($serializedData);
    ini_set('unserialize_callback_func', $originalCallback);
    
    /* your code here */
```

## Cryptographically secure algorithms

### General

MD2, MD4, MD5, SHA0, SHA1, DES, 3DES, RC2, RC4 algorithms are proven flawed or weak. Avoid using them when possible.

### mcrypt extension

The mcrypt extension is not maintained and has been deprecated since PHP 7.1, consider migrating to openssl.

Nevertheless if you are still using mcrypt, Php Inspections (EA Extended) finds several issues:
* MCRYPT_RIJNDAEL_192 and MCRYPT_RIJNDAEL_256 [are not AES-compliant](https://bugs.php.net/bug.php?id=47125);
* MCRYPT_RIJNDAEL_256 [is not AES-256](https://paragonie.com/blog/2015/05/if-you-re-typing-word-mcrypt-into-your-code-you-re-doing-it-wrong#title.1.2)

## Cryptographically secure randomness

For cryptographic operations purposes it's important to properly generate IV (Initialization Vector), which is used for 
further operations. In PHP you can use the following functions for IV generation: openssl_random_pseudo_bytes, mcrypt_create_iv 
and random_bytes.

The openssl_random_pseudo_bytes and mcrypt_create_iv functions require slightly different approaches, for example:

### openssl_random_pseudo_bytes

This code checks if a random value was cryptographically strong and if the value generation succeeded. 
```php
$random = openssl_random_pseudo_bytes(32, $isSourceStrong);
if (false === $isSourceStrong || false === $random) {
    throw new \RuntimeException('IV generation failed');
}
```

### mcrypt_create_iv

This code uses MCRYPT_DEV_RANDOM (available on Windows since PHP 5.3) which might block until more entropy is available 
and checks if the value generation succeeded.
```php
$random = mcrypt_create_iv(32, MCRYPT_DEV_RANDOM);
if (false === $random) {
    throw new \RuntimeException('IV generation failed');
}
```

## Variables extraction

### parse_str()

The parse_str function parses an encoded string as if it were the query string passed via a URL, and sets variables in the current 
scope (or in the array if the second parameter is provided). To stay safe, you should always provide the second parameter.
```php
parse_str($encodedString, $parsedValues);

/* now you can work with your data: $parsedValues['variable'] or $parsedValues['variable'][0] */
```

### extract()

The extract function imports variables from an array into the current scope. You can apply some rules to the extraction process by 
providing the second argument.
```php
/* an example: we are protecting existing scope from modification */

$countVariablesCreated = extract($values, EXTR_SKIP);
if ($countVariablesCreated != count($values)) {
    throw new \RuntimeException('Extraction failed: scope modification attempted');
}
```

## SSL server spoofing

### curl_setopt()

The curl_setopt function allows manipulation of CURLOPT_SSL_VERIFYHOST and CURLOPT_SSL_VERIFYPEER, responsible for SSL 
connection certificate validation (host name and CA information). Disabling the settings allows interception of SSL connections.

```php
/* case 1: debug/production environment */

curl_setopt($curlHandler, CURLOPT_SSL_VERIFYHOST, $debug ? 0 : 2);
curl_setopt($curlHandler, CURLOPT_SSL_VERIFYPEER, $debug ? 0 : 1);
/* ternary operator informs Php Inspections (EA Extended) about the case, no warnings will be reported */
```

```php
/* case 2: self-signed certificates */

/* clearly state that we do certificate validation */
curl_setopt($curlHandler, CURLOPT_SSL_VERIFYHOST, 2);
curl_setopt($curlHandler, CURLOPT_SSL_VERIFYPEER, 1);

/* use one of those options: a CA certificate or a directory containing multiple certificates */
curl_setopt($curlHandler, CURLOPT_CAINFO,  '<path>/ca.crt');
curl_setopt($curlHandler, CURLOPT_CAPATH , '<path>/');
```
 
## Security advisories

> Note: if the composer.lock file ships with your project, adding roave/security-advisories brings 0 deployment risks.

> Note: the inspection doesn't fire when package type is implicitly defined as library.

> Best practices: consider adding `composer update` into your CI/CD to get informed about security issues early.

Security Advisories is a Vulnerability Database from SensioLabs (https://security.sensiolabs.org/database) which can 
be integrated with your workflow in several ways.

One way is to use a Components Firewall (https://github.com/Roave/SecurityAdvisories) by adding just one line into 
your composer.json file. The firewall declares vulnerable components as conflicting and prevents their installation via 
Composer.

Php Inspection (EA Extended) suggests adding the firewall only if third-party components are being used: e.g. if 
composer.json declares "name" property as "your-company/product" all non-dev packages not starting with "your-company/" 
are considered third-party.

Php Inspection (EA Extended) also checks if dev-packages (e.g. PHPUnit) have been defined in "require" section instead of 
"require-dev".

## RSA oracle padding vulnerability

Padding oracle attack is an attack which is performed using the padding of a cryptographic message. In cryptography, 
variable-length plaintext messages often have to be padded (expanded) to be compatible with the underlying cryptographic 
primitive. The attack relies on having a "padding oracle" who freely responds to queries about whether a message is 
correctly padded or not. Padding oracle attacks are mostly associated with CBC mode decryption used within block ciphers. 
Padding modes for asymmetric algorithms such as OAEP may also be vulnerable to padding oracle attacks.

Please reference following links for more details:
- https://paragonie.com/blog/2016/12/everything-you-know-about-public-key-encryption-in-php-is-wrong (OpenSSL-specific)
- https://paragonie.com/blog/2015/05/if-you-re-typing-word-mcrypt-into-your-code-you-re-doing-it-wrong (mcrypt-specific)

## Basic malware patterns

> Note: The inspection is not a full-scale security scanner, specialized products are still the thing to go with.

The inspection is inspired by the following projects (which you can also use without IDE):
- https://github.com/gwillem/magento-malware-scanner
- https://github.com/elcodigok/wphardening

The inspection checks limited set of built-in PHP functions used by malware scripts and reports suspicious cases.

## Untrusted files inclusion

>Note: the inspection is disabled by default

The inspection spots include/require constructs with relative inclusion paths which are relying on include_path option.

The problem here is that include_path is an environment configuration and we shouldn't rely on it when dealing with 
application components.

Real-life example: application B deployed with all dependencies onto a server where include_path contains application A.
If both applications are shipped with the same component, the component for application B gets loaded from application A. 
This introduces broken deployment pipelines: any update of the component in application A can break/compromise application B.

There are multiple options for resolving the issue:

- migrate onto composer and remove include/require statements as much as possible (recommended)
- use `__DIR__` constant in inclusion paths: `require_once __DIR__ . '/include/something.php'` 
- introduce e.g. `APPLICATION_ROOT` constant and use it in inclusion paths: `require_once APPLICATION_ROOT . '/include/something.php'` 
