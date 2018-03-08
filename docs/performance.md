# Performance
This document covers changes to your code to improve php performance.

## Packed hashtable optimizations

> Note: please reference this [article](https://blog.blackfire.io/php-7-performance-improvements-packed-arrays.html) 
> for more technical details and real-life examples

PHP7 introduced significant improvements in performance and memory usage. One of improvements was an optimization of 
internals for associative arrays. To be more specific if an array has only integer keys organized in natural 
ascending order, then it will consume less memory and work a little bit faster with core array functions.

Php Inspections (EA Extended) checks array creation constructs and gives hints if found ways for enabling the 
optimizations.

## Slow array function used in loop

> Note: you might want to check benchmarks first - [one](https://gist.github.com/Ocramius/8399625), [two](https://github.com/kalessil/phpinspectionsea/issues/138#issuecomment-279457133)

Synopsis: merging arrays in a loop is slow and causes high CPU usage.

Let's start with an example demonstrating the case:
```php
    $options = [];
    foreach ($configurationSources as $source) {
        /* somethig happens here */
        $options = array_merge($options, $source->getOptions());
    }
```

In order to reduce execution time we can modify the code and perform the merge operation only once:
```php
    /* the inner empty array covers cases when no loops were made */
    $options = [[]];
    foreach ($configurationSources as $source) {
        /* somethig happens here */
        $options[] = $source->getOptions(); // <- yes, we'll use a little bit more memory
    }
    /* PHP below 5.6 */
    $options = call_user_func_array('array_merge', $options);

    /* PHP 5.6+: more friendly to refactoring as less magic involved */
    $options = array_merge(...$options);
```

## Foreach variables reference usage correctness

> Note: this inspection has settings.

The inspection analyzes foreach statements for using variables by reference. It promotes using values by reference if 
corresponding settings were applied and checks for possible side-effects. Main side-effect is that foreach variables 
are remaining after loop finishes.

When such variable is a reference, we can accidentally modify the original collection, like here:
```php
    $array = ['...'];
    foreach ($array as &$value) {
        /* somethig happens here */
    }
    
    /* somethig happens here as well */
    $value = null; /* $array now is [null] */
```

It'll be enough to place `unset($value)` after the foreach-loops to prevent the issue.

## Non-optimized arrays mapping

The inspection analyzes assignment statements and checks if both lef and right sides have repetitive functions and 
methods calls, which can be reduced by introducing a local variable.

```php
    /* Case 1: repetitive function calls */
    $array[trim($value)] = trim($value);
    
    /* instead we can use following approach */
    $normalizedValue = trim($value);
    $array[$normalizedValue] = trim($normalizedValue);
    
    /* Case 2: repetitive method calls */
    $array[$normalizer->normalize($value)] = $normalizer->normalize($value);
    
    /* instead we can use following approach */
    $normalizedValue = $normalizer->normalize($value);
    $array[$normalizedValue] = trim($normalizedValue);
```
