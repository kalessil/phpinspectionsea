# Exploiting unserialize

The vulnerability allows to remotely execute code or perform code/SQL injection by 
using unserialize() function (more details here: https://www.owasp.org/index.php/PHP_Object_Injection).

There are several options how to resolve the issue:

## Using JSON format

The approach is straightforward and include using json_decode()/json_encode() instead of serialization.
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


