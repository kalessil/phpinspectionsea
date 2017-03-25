## Non-null parameters default value

> Note: the inspection is deactivated by default.

The inspection reports usage of non-null default values in methods and functions. 
The aim is to improve code usability, by moving handling default logic into body.

```php
    /* original code: logic mixed into signature, not possible to use nullable types */
    function calculateRadial($radius, $pi = M_PI, $multiplier = 1) {
        return $radius * $pi * $multiplier;
    }
    
    /* code refactoring step 1 (triggered by the inspection) */
    function calculateRadial($radius, $pi = null, $multiplier = null) {
        return $radius * ($pi ?? M_PI) * ($multiplier ?? 1);
    }
    
    /* code refactoring step 2 (further hardening, perhaps the plugin will assist here in future) */
    function calculateRadial(float $radius, ?float $pi, ?float $multiplier): float {
        return $radius * ($pi ?? M_PI) * ($multiplier ?? 1);
    }
```

## Usage of the silence operator

> Note: the inspection has settings ("Content aware reporting", enabled by default).

> Please reference to corresponding [stackoverflow thread](http://stackoverflow.com/questions/136899/suppress-error-with-operator-in-php) for more details.

Using the [silence operator (@)](http://php.net/manual/en/language.operators.errorcontrol.php) is considered a bad practice. Nevertheless we consider following contexts legit:

```php
    /* context: result is stored, we assuming that failure result being checked */
    $result = @mkdir('...');

    /* context: result is returned, we assuming that failure result being checked */
    return @mkdir('...');

    /* context: strict comparison with false, in PHP core false generally returned in case of failures */
    $result = false === @mkdir('...');
    $result = false !== @mkdir('...');

    /* context: used as logical predicator, generally used techique comlimentary with e.g. race condition inspections */
    if (!@mkdir('...') && @mkdir('...')) { ... }
```