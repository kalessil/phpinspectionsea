
# Phar-incompatible 'realpath()' usage

The realpath() expands all symbolic links and resolves references to '/./', '/../' and extra '/' characters in 
the input path and returns the canonical absolute pathname. But what about streams, like phar://<file-path>?

In case of streams the realpath [will cause unexpected behaviour](https://bugs.php.net/bug.php?id=52769) and an 
alternative is using dirname() function instead.

```php
    /* examples affected by stream context */
    define ('PROJECT_ROOT', realpath(__DIR__ . '/../'));
    require_once realpath(__DIR__ . '/../../vendor/autoload.php');
    
    /* stream-safe alternatives */
    define ('PROJECT_ROOT', dirname(__DIR__) . '/');
    require_once realpath(dirname(dirname(__DIR__)) . '/vendor/autoload.php');
```