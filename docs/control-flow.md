## Non-optimal if conditions

> Note: the inspection has settings

> Note: the inspection documentation is not complete - some chapters are missing

### Literal and, or operators usage

By literal operators we mean `and`, `or` keyword used "instead" of `&&`, '||'. Instead is double quited for the same 
 reason, why we encourage to use `&&`, `||` - because of different operators precedence which differs.
 
Please reference to the [official documentation](http://php.net/manual/en/language.operators.logical.php) for more 
details. Following case should give you a common idea about side-effects:

```php
    $x = false || true;  // is ($x = (false || true)) => true
    $x = false or true;  // is (($x = false) or true) => false
    
    $x = true && false;  // is ($x = (true && false)) => false
    $x = true and false; // is (($x = true) and false) => true
```