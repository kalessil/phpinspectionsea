# Static local variables

> Note: the inspection is deactivated by default.

> Note: the inspection brings less benefit for PHP 5.6+ due to non-editable array optimizations.

Static variables, defined in a function/method are not used widely and in nutshell representing static property visible 
only inside the function/method. As for any static variable, it is stored in memory until script is executed.

From performance point of view the technique is a micro-optimization with following benefits:
- the variable is initialized only once;
- the variable reused on repetitive calls without new memory allocation;
- static variables can be used for replacing/hiding static class members;

# Packed hashtable optimizations

> Note: please reference this [article](https://blog.blackfire.io/php-7-performance-improvements-packed-arrays.html) 
> for more technical details and real-life examples

PHP7 introduced significant improvements in performance and memory usage. One of improvements was an optimization of 
internals for associative arrays. To be more specific if an array has only integer keys organized in natural 
ascending order, then it will consume less memory and work a little bit faster with core array functions.

Php Inspections (EA Extended) checks array creation constructs and gives hints if found ways for enabling the 
optimizations.