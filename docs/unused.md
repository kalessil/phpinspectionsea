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

## Unused constructor dependencies

Analyzes constructors in dependency injection context and reports stored but unused ones.

```php
    /* before */
    class Clazz {
        private $usedDependency;
        private $unusedDependency;

        public function __construct(object $usedDependency, object $unusedDependency) {
            $this->usedDependency   = $usedDependency;
            $this->unusedDependency = $unusedDependency;
        }

        public function getDependency(): object {
            return $this->usedDependency;
        }
    }

    /* after */
    class Clazz {
        private $usedDependency;

        public function __construct(object $usedDependency) {
            $this->usedDependency = $usedDependency;
        }

        public function getDependency(): object {
            return $this->usedDependency;
        }
    }
```

## Senseless proxy function

Reports if a method only content is a parent method call.

```php
    /* before */
    class ParentClass {
        public function method(string $parameter) {
            return $parameter;
        }
    }
    class ChildClass extends ParentClass {
        public function method(string $parameter) {
            return parent::method($parameter);
        }
    }

    /* after */
    class ParentClass {
        public function method(string $parameter) {
            return $parameter;
        }
    }
    class ChildClass extends ParentClass {
    }
```