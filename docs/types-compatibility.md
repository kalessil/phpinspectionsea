## Foreach source to iterate over

Checks foreach value, takes into account classes supporting iteration operations.

Here documented how to fix some of reported cases:

```php
    /* Case 1: `$items[1]` reported with `Expressions' type was not recognized, please check type hints.` message */
    
    /* @var $items string[][] <- the fix, $string type will be correctly recognized as `string` */
    preg_match_all('/.+/','.', $items); /* preg_match_all/preg_match stores results in $items */
    foreach ($items[1] as $string) {
        ...
    }
```