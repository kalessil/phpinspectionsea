# Exploiting unserialize

> When Php Inspection (EA Extended) reports the rule violations, ensure that it not belongs to any of dev-tools 
> (profilers, dev-environment specific caching and etc.). Dev-tools should not go to production and normally 
> do not have any security guarantee.

The vulnerability allows to remotely execute code or perform code/SQL injection by 
using unserialize() function (more details here: https://www.owasp.org/index.php/PHP_Object_Injection).

There are several options how to resolve the issue:

## Using JSON format

The approach is straightforward and includes using json_decode()/json_encode() instead of serialization.
Unfortunately it is not suitable for performance-optimized components.

## PHP 7.0.0+

Since PHP 7.0.0 the unserialize function has second parameter $options, which allows to 
implicitly specify which classes can be unserialized.

```php
    /* graceful approach with supporting older versions of PHP */ 
    if (PHP_VERSION_ID >= 70000) {
        /* to forbid classes unserializing at all use this: array('allowed_classes' => false) */
        $unserializedData = unserialize($serializedData, array('allowed_classes' => ['Class1', 'Class2']));
    } else {
        $unserializedData = unserialize($serializedData);
    }
```

## Hooking into unserialize callback

This technique allows to hook into class-loading during unserialize, making it possible to prevent new classes loading.

```php
    function __autoload($classname) {
        /* leave it empty to prevent further autoloading, only loaded classes will be available */
    }
    
    $originalCallback = ini_set('unserialize_callback_func', '__autoload');
    $unserializedData = unserialize($serializedData);
    ini_set('unserialize_callback_func', $originalCallback);
    
    /* your code here */
```

# Cryptographically secure algorithms

## General

MD2, MD4, MD5, SHA0, SHA1, DES, 3DES, RC2, RC4 algorithms are proven flawed or weak. Avoid using them when possible.

## mcrypt extension

The extension is not maintained and has been deprecated since PHP 7.1, consider migrating to openssl.

Nevertheless if you still using mcrypt, Php Inspections (EA Extended) finds several issues:
* MCRYPT_RIJNDAEL_192 and MCRYPT_RIJNDAEL_256 [are not AES-compliant](https://bugs.php.net/bug.php?id=47125);
* MCRYPT_RIJNDAEL_256 [is not AES-256](https://paragonie.com/blog/2015/05/if-you-re-typing-word-mcrypt-into-your-code-you-re-doing-it-wrong#title.1.2)

# Cryptographically secure randomness

For cryptographic operations purposes it's important to properly generate IV (Initialization Vector), which used for 
further operations. In PHP you can use following functions for IV generation: openssl_random_pseudo_bytes, mcrypt_create_iv 
and random_bytes.

Using openssl_random_pseudo_bytes and mcrypt_create_iv has own requirements, so let see how it should look like:

## openssl_random_pseudo_bytes

The code checks if random value was cryptographically strong and if the value generation succeeded. 
```php
$random = openssl_random_pseudo_bytes(32, $isSourceStrong);
if (false === $isSourceStrong || false === $random) {
    throw new \RuntimeException('IV generation failed');
}
```

## mcrypt_create_iv

The code uses MCRYPT_DEV_RANDOM (available on Windows since PHP 5.3) which might block until more entropy available 
and checks if the value generation succeeded.
```php
$random = mcrypt_create_iv(32, MCRYPT_DEV_RANDOM);
if (false === $random) {
    throw new \RuntimeException('IV generation failed');
}
```

# Variables extraction

## parse_str()

The function parses encoded string as if it were the query string passed via a URL and sets variables in the current 
scope (or in the array if second parameter is provided). To stay safe, you should always provide second parameter.
```php
parse_str($encodedString, $parsedValues);

/* now you can work with your data: $parsedValues['variable'] or $parsedValues['variable'][0] */
```

## extract()

The function imports variables from an array into the current scope. You can apply some rules to extraction process by 
providing second argument.
```php
/* an example: we are protecting existing scope from modification */

$countVariablesCreated = extract($values, EXTR_SKIP);
if ($countVariablesCreated != count($values)) {
    throw new \RuntimeException('Extraction failed: scope modification attempted');
}
```

# SSL server spoofing

## curl_setopt()

The function allows to manipulate CURLOPT_SSL_VERIFYHOST and CURLOPT_SSL_VERIFYPEER, responsible for SSL 
connection certificate validation (host name and CA information). Disabling the settings allows to intercept SSL connections.

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
 
# Security advisories

> Note: if the composer.lock is coming with you project, adding roave/security-advisories brings 0 deployment risks.

> Best practices: consider adding `composer update` into your CI/CD to get informed about security issues early.

Security Advisories is a Vulnerability Database from SensioLabs (https://security.sensiolabs.org/database), which can 
be integrated with your workflows in several ways.

One of ways is using a Components Firewall (https://github.com/Roave/SecurityAdvisories), by adding just one line into 
your composer.json file. The firewall declares vulnerable components as conflicting and not allows to install them via 
Composer.

Php Inspection (EA Extended) suggests adding the firewall only if third-party components being used: e.g. if 
composer.json declares "name" property as "your-company/product" all non-dev packages not starting with "your-company/" 
are considered third-party.