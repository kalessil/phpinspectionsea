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

# Class violates singleton/factory pattern definition

> Note: The singleton pattern is recognized by searching for the method `getInstance` in the class. In case of false-positives consider giving the method an expected name (`create`, `getEntityInstance`, `getBuiltEntity`, etc.)

The inspection searches for typical patterns violations:
- Factory: the constructor is protected, but create*/from* methods are not defined
- Singleton: the `getInstance` method exists and is public, but the constructor is not protected
