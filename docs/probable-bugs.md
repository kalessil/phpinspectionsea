
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
    require_once dirname(dirname(__DIR__)) . '/vendor/autoload.php';
```

# Addition operator applied to arrays

There are several ways of merging arrays in PHP: array_merge(), array_replace() and addition operators.
Each of them has own strategy of dealing with overriding values, specifically addition operators applicable in several 
scenarios.

```php
    /* case 1: merging configuration - make an option immutable */
    $forcedDefaults     = ['component' => 'component'];
    $userConfiguration  = ['component' => 'legacy_app_component'];
    
    $result = $forcedDefaults + $userConfiguration;
    // => ['component' => 'component']
```

```php
    /* case 2: merging configuration - make some options immutable (multiple sources) */
    $forcedDefaults               = ['persistent' => true];
    $productionCoreConfiguration  = ['persistent' => false, 'debug' => false];
    $userConfiguration            = ['persistent' => false, 'debug' => true];
    
    /* ensures that user configuration is not affecting persistence and debug modes */
    $result = $forcedDefaults + $productionCoreConfiguration + $userConfiguration;
    // => ['persistent' => true, 'debug' => false]
```

```php
    /* case 3: merging configuration - adding not configured options */
    $userConfiguration  = ['port' => '8888'];
    $defaults           = ['host' => '127.0.0.0', 'port' => '3386'];
    
    /* if user didn't configure something, add defaults */
    $result = $userConfiguration + $defaults;
    // => ['host' => '127.0.0.0', 'port' => '8888']
```

# Forgotten debug statements

> Note: you can register own debug methods in the inspections' settings (e.g. \My\Class::debug_method or \debug_function).

Forgotten debug statements can disclosure sensitive information, make serious impact to performance or break 
application in production environment (e.g. headers already sent warning).

Due to this we recommended to check carefully all reported cases. If you discovered a false-positive or a new case, 
don't hesitate [sharing with us](https://github.com/kalessil/phpinspectionsea/issues).

# Proper preg_quote() usage

The most common used separators in PHP are `/@#~`, which are not escaped by default (by default escaped `.\+*?[^]$(){}=!<>|:-`).
Though Php Inspections (EA Extended) reports all cases when preg_quote() call doesn't have the second argument.

Ignoring this PHP specific can lead to introducing bugs and even [vulnerabilities](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2016-5734).

# 'compact()' arguments existence

PHP is not warning if a 'compact()' was called referencing non-existent variable. A typo or refactoring might cost 
long debugging in this case.

# Class autoloading correctness

> Note: the inspection is based on PSR-0 and psr-4 autoloading Standards

From time to time we name class and the file containing it with typos, or probably renaming class without renaming its' 
file and breaking class autoloading. The inspection will spot class and file names mismatch before the issue popped up.