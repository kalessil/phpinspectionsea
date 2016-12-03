# Exploiting unserialize

The vulnerability allows to remotely execute code or perform code/SQL injection by 
using unserialize() function (more details here: https://www.owasp.org/index.php/PHP_Object_Injection).

There are several options how to resolve the issue:

## Using JSON format

The approach is straightforward and includes using json_decode()/json_encode() instead of serialization.
Unfortunately it is not suitable for performance-optimized components.

## PHP 7.0.0+

Since PHP 7.0.0 the unserialize function has second parameter $options, which allows to 
implicitly specify which classes can be unserialized.

## Hooking into unserialize callback

This technique allows to hook into class-loading during unserialize, making it possible to prevent new classes loading.

```php
    function __autoload($classname) {
        /* leave it empty to prevent further autoloading, only loaded classes will available */
    }
    
    $originalCallback = ini_set('unserialize_callback_func', '__autoload');
    $unserializedData = unserialize($serializedData);
    ini_set('unserialize_callback_func', $originalCallback);
    
    /* your code here */
```

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

## extract ()

The function imports variables from an array into the current scope. You can apply some rules to the extraction by 
providing second argument.
```php
/* One example: we are protecting existing scope from modification */

$countVariablesCreated = extract($values, EXTR_SKIP);
if ($countVariablesCreated != count($values)) {
    throw new \RuntimeException('Extraction failed: ');
}
```

 
