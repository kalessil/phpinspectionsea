# Control flow

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

## General Exception is thrown

### Throw \Exception instance

Since generic exceptions are not helpful when maintaining and debugging PHP-application, we would suggest using more 
specific [SPL exceptions](https://secure.php.net/manual/en/spl.exceptions.php).

### Exception is thrown without a message

Informative and reasonably detailed exception message saves debugging time a lot, hence we spot places where 
exception messages are not provided at all.

In case of well-structured domains, exception classes naming can be speaking itself. A couple ideas about raising them 
Php Inspections (EA Extended) friendly way:

```php
    /* exception parameters injection (builder approach) */
    throw (new UserNotFoundException())->withUserId($userId);
    
    /* custom constructor */
    throw new UserNotFoundException($userId);
```

## If-return-return could be simplified

The inspection finds places where conditional return-statements can be simplified (by Quick-Fixing), reducing code 
complexity metrics and amount of maintainable codebase. 

```php
    /* sample code fragment before applying Quick-Fix */
    if ($variable === 'value') {
        return true;
    }
    return false;
    
    /* sample code fragment after applying Quick-Fix */
    return $variable === 'value';
```

## Loop which does not loop

The inspection finds loop-constructs that are terminated with continue, break, throw or return in the first iteration. While 
this approach applicable to generators and iterable classes, in general it points to refactoring leftovers, bad merges 
and bugs.

> Note: while terminating statements can be preceded by other statements, the inspection still reports the pattern

## Switch-case could be simplified

The inspection finds switch-constructs which can be refactored into more suitable constructs (e.g. if-constructs), 
reducing cognitive load and clearer expressing code intention. 

```php
    /* sample code fragment reported by the the inspection */
    switch ($variable) {
        case 'value':
            /* operations: 1st batch */
            break;
        default:
            /* operations: 2nd batch */
            break;
    }

    /* sample code fragment after refactoring */
    if ($variable === 'value') {
        /* operations: 1st batch */
    } else {
        /* operations: 2nd batch */
    }
```

## Foreach usage possible

The inspection finds for-constructs which can be refactored (by Quick-Fixing) into foreach-constructs, reducing 
cognitive load, improving maintainability and enabling the analyzer to apply more checks.

> Note: foreach-statements are also well optimized for working with <a href="https://secure.php.net/manual/en/class.iterator.php">iterators</a>.

## Ternary operator could be simplified

Reports if ternary operator can be refactored to simply use the conditional variable as the result
(thus omitting the ternary branches). This reduces the both cyclomatic and cognitive code complexity.

```php
    /* sample code fragment before applying Quick-Fix */
    $variable = $number > 0 ? true : false;
    $variable = $number & $flag ? true : false;
    
    /* sample code fragment after applying Quick-Fix */
    $variable = $number > 0;
    $variable = (bool) ($number & $flag);
```
## Statement could be decoupled from foreach

Analyzes foreach loops and reports expressions that are not connected with the loop's scope. In the most cases such 
expression can be executed once outside of corresponding loop.

> Note: known false-positives are related to the input/output operations, executed in the loop - suppression recommended for this case

## 'gettype(...)' could be replaced with 'is_*(...)'

Reports 'gettype()' function usages, which can be replaced by 'is_array()' and similar functions. 
Such replacement is clearer expresses intention and more friendly to Static Code Analysis tools relying to types inference.