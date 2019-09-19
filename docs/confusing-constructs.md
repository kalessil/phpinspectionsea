# Confusing constructs

## Suspicious ternary operator

Reports if ternary operators only return operands of condition. In this case the ternary can be simplified.

```php
    /* before */
    $value = $argument === '' ? '' : $argument;
    
    /* after */
    $value = $argument;
```

## Method name matches existing field name

Reports when class method names overlap with fields names.

The case is not depending on overlapped field visibility and might really confuse when a field
stores value of callable type: '$this->fieldOrMethod()' might have intention of '($this->fieldOrMethod)()'.

## Nested ternary operator

Reports nested usage of ternary operators, which are mainly representing maintainability issues.

## Useless return

Analyzes return statements and reports multiple issues.

```php
    /* before */
    return $local = $value;
    
    /* after */
    return $value;
```

## Referencing objects

Analyzes callables' parameters and new expressions and reports when the object is being used in a reference context.

```php
    /* before */
    $object = & new Clazz();
    
    /* after */
    $object = new Clazz();
```
