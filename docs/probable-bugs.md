# Probable bugs
This document provides help on getting your code bug free.

## Phar-incompatible 'realpath()' usage

The [realpath()](http://php.net/manual/en/function.realpath.php) function expands all symbolic links and resolves references 
to '/./', '/../' and extra '/' characters in the input path and returns the canonical absolute pathname. But what about 
streams, like phar://<file-path>?

In case of streams the realpath [will cause unexpected behaviour](https://bugs.php.net/bug.php?id=52769). An 
alternative is to use the [dirname()](http://php.net/manual/en/function.dirname.php) function instead.

```php
    /* before */
    define ('PROJECT_ROOT', realpath(__DIR__ . '/../'));
    require_once realpath(__DIR__ . '/../../vendor/autoload.php');
    
    /* after */
    define ('PROJECT_ROOT', dirname(__DIR__) . '/');
    require_once dirname(dirname(__DIR__)) . '/vendor/autoload.php';
```

## Forgotten debug statements

> Note: you can register your own debug methods in the inspections' settings (e.g. \My\Class::debug_method or \debug_function).

Forgotten debug statements can disclose sensitive information, make serious impact to performance, or break 
applications in their production environment (e.g. headers already sent warning).

Due to this we recommended to check carefully all reported cases. If you discovered a false-positive or a new case, 
don't hesitate to [share it with us](https://github.com/kalessil/phpinspectionsea/issues).

## Proper preg_quote() usage

The most commonly used separators in PHP are `/@#~`, which are not escaped by default (escaped by default: `.\+*?[^]$(){}=!<>|:-`).
Though Php Inspections (EA Extended) does report all cases where the preg_quote() call doesn't have the second argument.

Ignoring this PHP detail can lead to the introduction of bugs and even [vulnerabilities](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2016-5734).

## 'compact()' arguments existence

PHP does not generate a warning if 'compact()' was called with a non-existent variable. A typo or refactoring might cost 
a long debugging session in this case.

## Class autoloading correctness

> Note: this inspection is based on PSR-0 and psr-4 autoloading standards.

From time to time we introduce typos in a class name or the file containing, probably when renaming a class without renaming its 
file. This breaks class autoloading. The inspection will spot class and file name mismatches before the issue pops up.

PHP´s magic [`::class`-constant](http://php.net/manual/en/language.oop5.basic.php#language.oop5.basic.class.class) will not 
canonical the casing of your imports.

This can lead to hard to debug errors when you a using a case sensitive `PSR-11`-locator service like `Zend\ServiceManager`.

```php
namespace FirstNamespace;

class TestClass {}
```

```php
namespace YetAnotherNamespace;

use FirstNamespace\Testclass; // wrong case in last segment

$instance = new TestClass(); // perfectly valid since php itself is not case sensitive for classnames

$container->get(TestClass::class); 
// TestClass::class expands to FirstNamespace\Testclass which is not registered in the $container
```
Discussion on Twitter: https://twitter.com/benjamincremer/status/872695045757038593  
Demo: https://3v4l.org/9uBKU

Please note that this is not a bug in PHP but expected behaviour: "The class name resolution using ::class is a compile 
time transformation. That means at the time the class name string is created, no autoloading has happened yet. As a consequence, 
class names are expanded even if the class does not exist. No error is issued in that case.". See: [php.net](http://php.net/manual/en/language.oop5.basic.php#language.oop5.basic.class.class)

## Null pointer exceptions prevention

> Note: the inspection is further developed in [Php Inspections (EA Ultimate)](https://plugins.jetbrains.com/plugin/16935-php-inspections-ea-ultimate-)
> where it covers more cases that described here.

This inspection name is clearly taken from Java. We're also actively enhancing this inspection towards similar checks in Java.

Php specific is taken into account, but some limitations are exists: 
- `is_object()`, `is_null()` and similar `is_*()` function calls are not analyzed, instead we recommend to rely on null
  identity and `instanceof` operators;
- this inspection doesn't rely on PhpDoc - types must be implicitly declared (parameters and return);
- this inspection targets only methods for performance reasons;

Following cases currently supported (we'll keep extending the list):
- method parameters (nullable objects), e.g. `public function method(?\stdClass $first, \stdClass $second = null) { ... }`;
- method local variables initialized with potentially nullable values;

## 'mkdir(...)' race condition

This issue is difficult to reproduce, as any concurrency-related issues are.
It appears when several processes are attempting to create a directory which does not
yet exist. Specifically, when one process is between `is_dir()` and `mkdir()` after
another process has already managed to create the directory.

```php
    /* before */
    if (!is_dir($directory)) {
        mkdir($directory);
    }
    
    /* after */
    if (! is_dir($directory) && ! mkdir($directory) && ! is_dir($directory)) {
        throw new \RuntimeException(sprintf('Directory "%s" was not created', $directory));
    }
```

## 'file_put_contents(...)' race condition

> Note: this inspection is part of [Php Inspections (EA Ultimate)](https://plugins.jetbrains.com/plugin/16935-php-inspections-ea-ultimate-).

> Note: in order to report all cases, please check the inspection settings.

> Note: in Vagrant environments, using LOCK_EX might cause warnings: `file_put_contents(): Exclusive locks are not supported for this stream`
> , which can be solved [in multiple ways](https://github.com/thephpleague/flysystem/issues/445#issuecomment-191160239)

This issue is similar to the `'mkdir(...)' race condition` case, but by default we are reporting only cases when php-content 
is written into a file - in this case concurrency issues can result in compromised or corrupted application state.

## Continue misbehaviour in switch

This inspection analyzes continue usage in switch-case context. Due to PHP's 
[unusual continue syntax](https://secure.php.net/manual/en/control-structures.continue.php), new users who are used to 
other languages might be confused by its behaviour. 

Covers following case:
```php
foreach ([] as $value) {
    switch ($value) {
        case 'value':
            continue; /* behaves as 'break', 'continue 2' was intended */
        /* more code here */
    }
} 
```

This is PHP-specific and mentioned in the [documentation](https://secure.php.net/manual/en/control-structures.continue.php):

> In PHP the switch statement is considered a looping structure for the purposes of continue. continue behaves like break 
> (when no arguments are passed). If a switch is inside a loop, continue 2 will continue with the next iteration of the outer loop.

## Empty/isset results correctness

The inspection is targeting isset(...) and empty(...) constructs applied to class property references. If the property 
could not be resolved, the `__isset` method existence is verified and reported if missing.

The main assumption made here is that the codebase is not relying on dynamic properties, but on DTOs (Data Transfer Objects).
With this assumption the magic methods `__isset` (we check only this one), `__get`, `__set` are needed.

If the `__isset` method is not implemented, then both target constructs will constantly return `false`.

## Incorrect random generation range

Reports random number generation constructs where range is not specified correctly.

```php
    /* the following case will be reported */
    $number = random_int(PHP_INT_MAX, PHP_INT_MIN);
```

## Suspicious loop

Analyzes for and foreach loops and reports following cases:
- overriding outer loops variables 
- overriding function or method parameters
- suspicious conditions
