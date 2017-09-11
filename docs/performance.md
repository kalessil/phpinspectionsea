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

Synopsys: merging arrays in a loop causes high CPU usage and takes pretty much time for execution.

Let's start with an example demonstrating the case:
```php
    $options = [];
    foreach ($configurationSources as $source) {
        /* more logic here */

        $options = array_merge($options, $source->getOptions());
    }
```

In order to reduce execution time we can modify the code and perform the merge operation only once:
```php
    /* the inner empty array covers cases when no loops were made */
    $options = [[]];
    foreach ($configurationSources as $source) {
        /* more logic here */

        $options[] = $source->getOptions(); // <- yes, we'll use a little bit more memory
    }
    /* PHP below 5.6 */
    $options = call_user_func_array('array_merge', $options);

    /* PHP 5.6+: more friendly to refactoring as less magic involved */
    $options = array_merge(...$options);
```
