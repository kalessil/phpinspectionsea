# Architecture

This document covers various architectural changes that you might want to apply to your code base and how we suggest you approach them while using **Php Inspections**.

## Class overrides a field of a parent class

In general the issue is due to name collision. Nevertheless in the context of inheritance, it is better to verify the reported cases.

Reported cases and possible alternatives:

* Parent's property is private, children have any visibility (suggested declaring as protected);
    * rename the property
    * change the property visibility to protected (try to avoid this as the field becomes accessible to all children and prefer the private visibility over the protected visibility)
    * add and use setters/getters to keep it private but accessible/mutable
* Children declare the same visibility as their parent's property (suggested using initialization in a constructor)
    * rename the property
    * move property initialization into the constructor
    * use @property annotation to re-define type-hints

## Long inheritance chain

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

## Class violates singleton/factory pattern definition

> Note: The singleton pattern is recognized by searching for the method `getInstance` in the class. In case of false-positives consider giving the method an expected name (`create`, `getEntityInstance`, `getBuiltEntity`, etc.)

The inspection searches for typical patterns violations:
- Factory: the constructor is protected, but create*/from* methods are not defined
- Singleton: the `getInstance` method exists and is public, but the constructor is public

## Multiple return statements usage

> Note: the inspection is deactivated by default, but highly recommended for enabling

> Note: the inspection applies only to class methods (traits and regular functions are not analyzed)

> Please reference to corresponding [stackoverflow thread](https://stackoverflow.com/questions/36707/should-a-function-have-only-one-return-statement) 
> for more details.

A quote from the plugin community member: `There is absolutely nothing wrong with multiple return statements!?!`. 
At first look it can sound confusing, but multiple return statements indicating violations of single responsibility principe.
Let's calculate how many test we need to write for this method:
```php
    /**
     * Type-safe method, checks if a string is empty. Not types casting magic allowed.
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
for string with space characters and at least for a string with content.

Means we have a design problem. E.g. in this case we can ask ourselves questions like: 
"why is this function have additional validation", "why the parent workflow can not guarantee strings to be dispatched",
"why it's not 2 functions: isEmptyString, isEmptyStringTypeSafe" and so on.

Prom this angle the inspection can spot existing architecture issues and alert when you going to introduce them during 
development.




