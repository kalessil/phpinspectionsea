# Non-null parameters default value

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
        return $radius * ( $pi ?? M_PI ) * ( $multiplier ?? 1 );
    }
    
    /* code refactoring step 2 (further hardening, perhaps the plugin will assist here in future) */
    function calculateRadial(float $radius, ?float $pi, ?float $multiplier): float {
        return $radius * ( $pi ?? M_PI ) * ( $multiplier ?? 1 );
    }
```