# Probable bugs
This document provides help on getting your code bug free.

## Phar-incompatible 'realpath()' usage

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

## Addition operator applied to arrays

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

## Forgotten debug statements

> Note: you can register own debug methods in the inspections' settings (e.g. \My\Class::debug_method or \debug_function).

Forgotten debug statements can disclosure sensitive information, make serious impact to performance or break 
application in production environment (e.g. headers already sent warning).

Due to this we recommended to check carefully all reported cases. If you discovered a false-positive or a new case, 
don't hesitate [sharing with us](https://github.com/kalessil/phpinspectionsea/issues).

## Proper preg_quote() usage

The most common used separators in PHP are `/@#~`, which are not escaped by default (by default escaped `.\+*?[^]$(){}=!<>|:-`).
Though Php Inspections (EA Extended) reports all cases when preg_quote() call doesn't have the second argument.

Ignoring this PHP specific can lead to introducing bugs and even [vulnerabilities](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2016-5734).

## 'compact()' arguments existence

PHP is not warning if a 'compact()' was called referencing non-existent variable. A typo or refactoring might cost 
long debugging in this case.

## Class autoloading correctness

> Note: the inspection is based on PSR-0 and psr-4 autoloading standards

From time to time we name class and the file containing it with typos, or probably renaming class without renaming its' 
file and breaking class autoloading. The inspection will spot class and file names mismatch before the issue popped up.

PHP´s magic [`::class`-constant](http://php.net/manual/en/language.oop5.basic.php#language.oop5.basic.class.class) will not canonical the casing of your imports.

This can lead to hard to debug errors when you a using a case sensitive service  `PSR-11`-locator like `Zend\ServiceManager`.


```php
namespace FirstNamespace;

class TestClass {}
```

```php
namespace YetAnotherNamespace;

use FirstNamespace\Testclass; // wrong case in last segment

$instance = new TestClass(); // perfectly valid since php itself is not case sensitive for classnames

$container->get(TestClass::class); 
// TestClass::class expands to FirstNamespace\Testclass which is not registered in the $conainer
```
Discussion on Twitter: https://twitter.com/benjamincremer/status/872695045757038593  
Demo: https://3v4l.org/9uBKU

Please note that this is not a bug in PHP but expected behaviour: "The class name resolution using ::class is a compile time transformation. That means at the time the class name string is created no autoloading has happened yet. As a consequence, class names are expanded even if the class does not exist. No error is issued in that case.". See: [php.net](http://php.net/manual/en/language.oop5.basic.php#language.oop5.basic.class.class)

## Null pointer exceptions prevention

The inspection name is clearly taken from Java, we also actively enhancing the inspection towards similar checks in Java.

Php specific is taken into account, but some limitations are exists: 
- is_object(), is_null() and similar is_*() functions calls are not analyzed, instead we recommend to rely on null 
  identity and instanceof operators;
- the inspection doesn't rely on PhpDoc - types must be implicitly declared (parameters and return);
- the inspection targets only methods for performance reasons;

Following cases currently supported (we'll keep extending the list):
- method parameters (nullable objects), e.g. `public function method(?\stdClass $first, \stdClass $second = null) { ... }`;
- method local variables initialized with potentially nullable values;
- to be continued (when funding is found);

## 'mkdir(...)' race condition

This issue is difficult to reproduce, as any of concurrency-related issues.
It appears when several processes are attempting to create a directory which is not
yet existing, but between is_dir() and mkdir() calls another process already
managed to create a directory.

Apart of different code constructs which vulnerable to this issue type, the safe 
variant is following: `!is_dir($folder) && !mkdir($folder) && !is_dir($folder)`.

## 'file_put_contents(...)' race condition

> Note: the inspection is part of Php Inspections (EA Ultimate)

> Note: in order to report all cases, please check the inspection settings

> Note: in Vagrant environment using LOCK_EX might cause warnings: `file_put_contents(): Exclusive locks are not supported for this stream`
> , which can be solved [in multiple ways](https://github.com/thephpleague/flysystem/issues/445#issuecomment-191160239)

This issue is similar to `'mkdir(...)' race condition` case, but by default we are reporting only cases when php-content 
is written into a file - in this case concurrency issues can result into compromised or corrupted application state.