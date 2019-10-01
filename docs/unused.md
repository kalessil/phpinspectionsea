# Unused

This document covers various cases of dead code which can be found in your code base while using **Php Inspections**.

## Parameter/variable is not used

Analyzes functions and methods control flow, reports unused variables and potentially unused variables (such
variables are used in write context only).

While the inspection reports multiple cases, one of them is following:

```php
    function name (array $array) {
        $array[] = '...';

        /* some logic here, which is not using $array */
    }
```

## Useless unset

Unset operations applied to callable parameters only destroy local copies/links. When parameters are not re-used, 
this operation can be safely removed.

```php
    function name (string $string) {
        /* some logic here, which is using $string */

        unset($string);

        /* some logic here, which is not using $string */
    }
```

## Class property initialization flaws

Reports class properties explicitly initialized with NULL and redundant assignments in constructors.
Class property default value is NULL if not specified, assigning it explicitly is redundant.

```php
    /* before */
    class Clazz {
        private $property = null;

        public function __construct() {
            $this->property = null;
        }
    }

    /* after */
    class Clazz {
        /* @var string|null */
        private $property;
    }
```