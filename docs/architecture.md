# Architecture

This document covers various architectural changes that you might want to apply to your code base and how we suggest you approach them while using **Php Inspections**.

## Class overrides a field of a parent class

In general the issue is due to name collision. Nevertheless in the context of inheritance, it is better to verify the reported cases.

Reported cases and possible alternatives:

* Children declare the same visibility as their parent's property (suggested using initialization in a constructor)
    * rename the property
    * move property initialization into the constructor
    * use @property annotation to re-define type-hints

* Parent's property is private, children have any visibility (suggested declaring as protected);
    * rename the property
    * change the property visibility to protected (try to avoid this as the field becomes accessible to all children and prefer the private visibility over the protected visibility)
    * add and use setters/getters to keep it private but accessible/mutable

### Common cases

```php
class WhichRedeclaresProperties extends BaseClass {
    /** @var NewTypeHint */
    protected $sameProperty;
}
```
can be refactored to similar code (but with clearer code navigation, analysis and debugging capabilities)
```php
/**
 * @property NewTypeHint $sameProperty
 */
class WhichRedeclaresProperties extends BaseClass {
}
```

## Long inheritance chain

> Note: the inspection is context aware and also takes into account Zend Framework 2+, PhpUnit and Yii 2+ specific.

In OOP, inheritance is one of the basic principles which allows to design and build well-structured components. As any other principle, inheritance might be applied in contexts where it's not suitable. In this case, we often can see long inheritance chains leading to class coupling within the hierarchy.

To avoid growing coupled inheritance relations, consider combining inheritance with a principle called [Composition over inheritance](https://en.wikipedia.org/wiki/Composition_over_inheritance).

## Overriding deprecated methods

Deprecation of APIs is a natural part of software lifecycle and consists of 2 steps: deprecation itself and removal of interfaces. If a deprecated method has been implemented or overridden, it is very likely that a problem with child components will arise in the near future (when the API is removed completely).

The consequences of removing a method are:
* Dead code is introduced (the method is not part of the parent workflow anymore) (increasing technical debt)
* Child components may break (high delivery risks)
* Applications will work with outdated components in order to avoid updating their code (high security risks)

Since none of these consequences are positive, the following workflow might be helpful:
* Deprecate methods by adding `@deprecated` to the method's annotation
* Run PHPStorm inspection tool and analyze where the deprecated method is used and plan to refactor your code

## Multiple return statements usage

> Note: the inspection is deactivated by default

> Note: the inspection applies only to class methods (traits and regular functions are not analyzed)

> Please reference to corresponding [stackoverflow thread](https://stackoverflow.com/questions/36707/should-a-function-have-only-one-return-statement) 
> for more details.

A quote from the plugin community member: `There is absolutely nothing wrong with multiple return statements!?!`. 
At first look it can sound confusing, but multiple return statements indicating violations of single responsibility principe.
Let's calculate how many test we need to write for this method:
```php
    /**
     * Type-safe method, checks if a string is empty. No types casting magic allowed.
     * 
     * @var mixed $argument
     * @return bool
     */
    public function isEmptyString($argument) {
        if (null === $argument) {
            return false;
        }
        
        if (!is_string($argument)) {
            return false;
        }
        
        return '' === trim($argument);
    }
```

To test the method we need at least 5 tests: for null, for non-string type, for empty string, 
for string with space characters only and at least one for a string with content.

Means we have a design problem. E.g. in this case we can ask ourselves questions like: 
"why does this function have additional validation inside", "why the parent workflow doesn't guarantee string type to be dispatched",
"why it was not introduced 2 functions here: isEmptyString, isEmptyStringTypeSafe" and so on.

From this angle the inspection can spot existing architecture issues and alert when you about to introduce them during development.

## Transitive dependencies usage

> Note: this inspection is part of [Php Inspections (EA Ultimate)](http://plugins.jetbrains.com/plugin/10215-php-inspections-ea-ultimate-).

> Note: it also worth checking [ComposerRequireChecker](https://github.com/maglnet/ComposerRequireChecker) for CI purposes

The inspection reports transitive dependencies usages when composer is being used as the package manager.

Transitive dependencies (classes from packages which are not required in application manifest) are managed by implicitly 
declared dependencies and might be dropped or updated to incompatible/compromised version at any time when application 
dependencies being updated.

The issue can be resolved by adding the transitive dependency into require/require-dev section of applications' 
composer.json file.  

## Badly organized exception handling

Analyzes try-catch constructs using a Clean Code approach. Refactoring the findings can greatly improve code maintainability.

The inspection reports multiple issue types, but let's take a case with more than 3 statements in try-block.
From the clean code point of view such block has to be refactored:
- unrelated statements should be moved to outer scope
- the related statements should be moved into a method, representing the use-case (ideally representing a micro-transaction)

```php
    /* before */
    try {
        $variable = '...';
        $variable = $actor->normalize($variable);
        $variable = $actor->validate($variable);
        $variable = $actor->process($variable);
    } catch (\RuntimeException $failure) {
        /* exception handling here */
    }
    
    /* after */
    $variable = '...';
    try {
        $variable = $actor->wellNamedMethodExplainingIntention($variable);
    } catch (\RuntimeException $failure) {
        /* exception handling here */
    }
```

## Callable parameter usage violates definition

Analyzes functions and methods parameters usage, verifying multiple cases:
- 'is_*(...)' calls against parameter type
- assigning new values to parameter against parameter type

> Note: when parameter is annotated as 'mixed', consider revising it to set specific type (the inspection skips analysis if finds 'mixed').

> Note: this inspection is disabled by default.

```php
    function (string $string, array $array) {
        /* gets reported, as we assigning string into originally array variable */
        $array = '...'; 
        
        /* gets reported, making no sense (always false in fact) */
        if (is_array($string)) {
            /* something happends here */
        }

        /* gets reported, making no sense (always true in fact) */
        if (is_string($string)) {
            /* something happends here */
        }
    }
```

## Empty class

Reports empty classes, which are normally should not exists.

> Note: in order to keep such classes, make sure that parent class is abstract. You can deprecate the class as well.

## Class implements interfaces multiple times

Reports if the class duplicates any of its parent interfaces in its 'implements' definition.

The case appears as refactorings left-overs or as indicator of application complexity is getting out of control.


```php
    /* before */
    interface Constract {}
    class ParentClass implements Contract {}
    class ChildClass extends ParentClass implements Contract {}

    /* after */
    interface Constract {}
    class ParentClass implements Contract {}
    class ChildClass extends ParentClass {}
```