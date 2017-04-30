## Non-optimal if conditions

> Note: the inspection has settings

> Note: the inspection documentation is not complete - some chapters are missing

### Literal and, or operators usage

By literal operators we mean `and`, `or` keywords used "instead" of `&&`, `||`. Instead is double quoted for the same 
 reason why we encourage you to use `&&`, `||` - because of the operators precedence which differs.
 
Please reference to the [official documentation](http://php.net/manual/en/language.operators.logical.php) for more 
details. Following example should give you a common idea about side-effects:

```php
    $x = false || true;  // is ($x = (false || true)) => true
    $x = false or true;  // is (($x = false) or true) => false
    
    $x = true && false;  // is ($x = (true && false)) => false
    $x = true and false; // is (($x = true) and false) => true
```

## PDO API usage

> Note: this chapter describes not yet released behaviour and quick-fixes 

The inspection reports following cases:

```php
    /* case 1: PDO:query() can be used instead */
    $statement = $pdo->prepare('...');
    /* the prepared statement executed immediately and without parameters and result check */
    $statement->execute(); 
```

The case doesn't have any parameters binding and the prepared statement is not really make any sense. 
Also the result of the query execution is not even checked. Following changes can be applied to fix the case:

- Apply a Quick-Fix to use PDO:query() instead
- Bind arguments while executing the statement
- Validate the statement execution result

```php
    /* case 2: PDO:exec() can be used instead - result statement of PDO:query() is not used */
    $pdo->query('...');
```

The case will consume system resources while the constructed statement is not used at all.
Following changes can be applied to fix the case:

- Apply a Quick-Fix to use PDO:exec() instead
- Use the returned statement
