# Code style

## Non-null default value parameters

> Note: this inspection is not active by default.

The inspection reports usage of non-null default values in methods and functions. 
The aim is to improve code usability by moving default logic into the body.

```php
    /* original code: logic is mixed in the signature, not possible to use nullable types */
    function calculateRadial($radius, $pi = M_PI, $multiplier = 1) {
        return $radius * $pi * $multiplier;
    }
    
    /* code refactoring step 1 (triggered by the inspection) */
    function calculateRadial($radius, $pi = null, $multiplier = null) {
        return $radius * ($pi ?? M_PI) * ($multiplier ?? 1);
    }
    
    /* code refactoring step 2 (further hardening, perhaps the plugin will assist here in the future) */
    function calculateRadial(float $radius, float $pi = null, float $multiplier = null): float {
        return $radius * ($pi ?? M_PI) * ($multiplier ?? 1);
    }
```

## Access modifiers shall be defined

> Note: this inspection has settings.

The inspection reports missing classes (incl. interfaces and traits), members (properties and methods), and access modifiers.

When modifiers are missing, visibility defaults to public access. This breaks encapsulation principles and leads to the coupling 
of application components, reducing control over object state and public APIs. The golden rule here is to make everything 
private by default. Relaxing visibility to protected and public members should only be done when it's required by the architecture.
