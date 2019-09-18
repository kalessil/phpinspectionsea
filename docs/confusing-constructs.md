# Confusing constructs

## Suspicious ternary operator

Reports if ternary operators only return operands of condition. In this case the ternary can be simplified.

```php
    /* before */
    $value = $argument === '' ? '' : $argument;
    
    /* after */
    $value = $argument;
```