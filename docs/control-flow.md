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
Also, the result of the query execution is not even checked. Following changes can be applied to fix the case:

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
    /* before */
    if ($variable === 'value') {
        return true;
    }
    return false;
    
    /* after */
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
    /* before */
    switch ($variable) {
        case 'value':
            /* operations: 1st batch */
            break;
        default:
            /* operations: 2nd batch */
            break;
    }

    /* after */
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
    /* before */
    $variable = $number > 0 ? true : false;
    $variable = $number & $flag ? true : false;
    
    /* after */
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

## 'unset(...)' constructs can be merged

Analyzes if the 'unset(...)' function was called sequentially. This can be safely replaced
with the 'unset(..., ...[, ...])' construction.

```php
    /* before */
    unset($variable);
    unset($argument);
    
    /* after */
    unset($variable, $argument);
```

## One-time use variables

In some cases variables are used as temporary value containers and it's possible to omit such variables at all.

```php
    /* before */
    $object = new Clazz();
    return $object->method();
    
    /* after */
    return (new Clazz())->method();
```

## 'list(...) = ' usage possible

Since PHP 5.5 it's possible to use mass-assignments from array with 'list($variable, ...) = $array'. 
Additionally, to code compactness and performance, you'll notice if data array contains any un-used entries.

```php
    /* before */
    $array  = [ ... ];
    $first  = $array[0];
    $second = $array[1];
    
    /* after */
    list($first, $second) = [ ... ];
```

## 'array_search(...)' could be replaced by 'in_array(...)'

Reports if 'array_search(...)' is used in the context of 'in_array(...)', where overhead lies in storing and returning index, 
which is not used in case of 'in_array(...)'. Also reported cases are misleading and refactoring with 'in_array(...)' would clearer
express intention of code constructs.

```php
    /* before */
    if (array_search($what, $where, true) === false) {
        /* some logic here */
    }
    
    /* after */
    if (! in_array($what, $where, true)) {
        /* some logic here */
    }
```

## 'strtr(...)' could be replaced with 'str_replace(...)'

Reports if 'strtr(...)' is used in the context of 'str_replace(...)'. Reported cases are misleading and refactoring with 
'str_replace(...)' would clearer express intention of code constructs.

```php
    /* before */
    $normalizedPath = strtr($string, '\\', '/');
    
    /* after */
    $normalizedPath = str_replace('\\', '/', $string);
```

## 'substr(...)' could be replaced with 'strpos(...)'

'substr(...)' invokes additional memory allocation (more work for GC), which is not needed in the context.
'strpos(...)' will do the same job with CPU resources only (assuming you are not dealing with big text fragments).

```php
    /* before */
    $containsString = substr($where, 0, strlen($what)) === $what;
    
    /* after */
    $containsString = strpos($where, $what) === 0;
```

> If you are eager for performance in your project this inspection in combination with "Fixed-time string starts with checks" 
> (which is disabled by default) would be a good option.

## Strings normalization

The inspection checks order of string case and length manipulations and reports anomalies.

```php
    /* before */
    $normalizedString = trim(strtolower($string));
    
    /* after */
    $normalizedString = strtolower(trim($string));
```

## Redundant 'else' keyword

Certain if-else conditions can have their else clause removed without changing the semantic.

In the example below, the 'else' statement can be safely removed because its 'if' clause returns a value.
Thus, even without the 'else/elseif', there’s no way you’ll be able to proceed past the if clause body.

```php
    /* before */
    if ($value !== 'forbidden-value') {
        return $value;
    } else {
        return null;
    }

    /* after */
    if ($value !== 'forbidden-value') {
        return $value;
    }
    return null;
```

> Please reference to corresponding <a href="https://softwareengineering.stackexchange.com/questions/122485/elegant-ways-to-handle-ifif-else-else">stackoverflow thread</a> for more details.

## Inverted 'if-else' constructs

> Note: this inspection is disabled by default as it might conflict with "Redundant 'else' keyword" inspection and guard clauses code style

Certain if-else conditions can have their conditions unnecessary inverted, so removing inversion and exchanging branches 
content is not changing the semantic.

```php
    /* before */
    if (! $rule->validate()) {
        /* failed validation logic here */
    } else {
        /* passed validation logic here */
    }

    /* after */
    if ($rule->validate()) {
        /* passed validation logic here */
    } else {
        /* failed validation logic here */
    }
```

## Unnecessary string case manipulation

In some cases string case manipulation is not necessary.

```php
    /* before */
    $matched = preg_match('/^prefix/i', strtolower($string));
    
    /* after */
    $matched = preg_match('/^prefix/i', $string);
```

## 'array_unique(...)' can be used

We were promoting usage of 'array_count_values(...)' instead of 'array_unique(...)' in some contexts previously, but
the 'array_unique(...)' was optimized and we are now suggesting the opposite way.

See this <a href="https://github.com/kalessil/phpinspectionsea/issues/434">thread</a> for more details.

```php
    /* before */
    $values = array_keys(array_count_values($array));
    $count = count(array_count_values($array));
    
    /* after */
    $values = array_values(array_unique($array));
    $count = count(array_unique($array));
```

## 'compact(...)' can be used

> Note: this inspection is disabled by default as using 'compact(...)' is not a common practice

Suggests using 'compact(...)' when it makes sense, normally this happens in context of dispatching arguments into templates.

```php
    /* before */
    $array = ['first' => $first, 'second' => $second];
    
    /* after */
    $array = compact('first', 'second');
```

## 'isset(...)' constructs can be merged

The inspection is advising when multiple 'isset(...)' statements can be merged into one.

```php
    /* before */
    $value = isset($first) && isset($second);
    
    /* after */
    $value = isset($first, $second);
```

## 'strlen(...)' misused

Analyzes 'strlen(...)' usages and reports if the construction can be replaced with empty string comparison.

```php
    /* before */
    if (strlen($string))   {}
    if (! strlen($string)) {}
    
    /* after */
    if ($string !== '') {}
    if ($string === '') {}
```

## 'ob_get_clean()' can be used

Analyzes and reports code construct, which could use 'ob_get_clean()' function.

```php
    /* before */
    $content = ob_get_contents();
    ob_end_clean();

    /* after */
    $content = ob_get_clean();
```
