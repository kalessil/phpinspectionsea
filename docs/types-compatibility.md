# Types compatibility

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

## 'empty(...)' usage

> Note: the inspection has settings, most of them deactivated by default

> Note: usage of 'empty(...)' still makes sense when big arrays checked for emptiness due to better performance.

> Note: more information why empty usage should be avoided: [here](https://www.toptal.com/php/10-most-common-mistakes-php-programmers-make#common-mistake-10-misusing-empty)

Here some examples which we hope will encourage you to stop using empty at all:

```php
    /* Case 1: inconsistent data types support */
    var_dump(empty([]));                // => bool(true), as expected 
    var_dump(empty(new ArrayObject())); // => bool(false), surprise-surprise =)
    
    /* Case 2: not working as expectid with magic classes */
    class RegularClass
    {
        public $property = 'value';
    }
    class MagicClass
    {
        private $values = ['property' => 'value'];
    
        public function __get($key)
        {
            if (array_key_exists($key, $this->values)) {
                return $this->values[$key];
            }
        }
        
        /* __set, __isset are omitted for demonstration purpose */
    }
    $regular = new RegularClass();
    $magic = new MagicClass();
    var_dump($regular->property);        // => string(5) "value"
    var_dump($magic->property);          // => string(5) "value"
    
    /* correctly implemented __isset resolves the issue demonstrated below */
    var_dump(empty($regular->property)); // => bool(false), as expected
    var_dump(empty($magic->property));   // => bool(true), surprise-surprise =)
```

## Strict comparison

Due to php loose typing and [type juggling](http://php.net/manual/de/language.types.type-juggling.php) you
can get unexpected results when comparing two variables. More type safe operations can improve your code
and avoid you head aches. PHP7 [new features towards typing](http://php.net/manual/en/migration70.new-features.php)
shows it.

Examples illustrating the above exposed can be found [here](http://www.phptherightway.com/pages/The-Basics.html#comparison-operators). In
there you can found also relevants resources explaining how php comparisson works.

Take into account that while loose typing in PHP is a powerfull feature. As any powerfull feature needs care when dealing with it.

## Strict type search in arrays

This inspection states that when the context requires strict types checking, then the third parameter flag should be used. Because
of the same explanation exposed in previous section, there will be cases in which you need to do strict type cheking here.

In order to emphasize on this, lets read more carefully the _WARNING_ that comes with documentation of **array_search** 
[return value](http://php.net/manual/en/function.array-search.php#refsect1-function.array-search-returnvalues). Do you see how it is referenced
again the issue with comparisons against loose typed variables?
