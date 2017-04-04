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
    
    /* correctly implemented __isset resolvea the issue demonstrated below */
    var_dump(empty($regular->property)); // => bool(false), as expected
    var_dump(empty($magic->property));   // => bool(true), surprise-surprise =)
```