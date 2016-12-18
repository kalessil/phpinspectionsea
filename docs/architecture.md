# Architecture

## Class overrides a field of a parent class

In general the case is about naming collision, nevertheless in context of inheritance better to re-check reported case.

Reported cases and possible alternatives:

* Parent's property is private, children has any visibility (suggested declaring as protected);
    * rename property;
    * make it protected (try avoiding this, as the field become accessible);
    * add setters/getters to keep it private;
* Children declares same access level as parent's property (suggests using constructor for initialization);
    * rename property;
    * move initialization into constructor;

## Long inheritance chain

In OOP, inheritance is one of basic principals which allows to design and build well-structured components. As any 
other principle inheritance might be applied to contexts where it's not suitable. In this case we often can see long 
inheritance chains leading to coupling classes in the hierarchy.
  
To avoid growing coupled inheritance relations, consider combining inheritance with a principle called 
[Composition over inheritance](https://en.wikipedia.org/wiki/Composition_over_inheritance).

## Overriding deprecated methods

Deprecation of APIs is a natural part of software lifecycle and consists of 2 steps: deprecation itself and removal of 
interfaces. If a deprecated method has been implemented or overridden, this guarantees a problem with child components 
in near future (when API is removed completely).

Removal consequences are:
* Dead code introduced (the method is not part of parent workflow anymore) (increasing tech. debt)
* Child components got broken (high delivery risks)
* Application bounded to work with outdated components (high security risks)

Since none of those options is pleasant, this workflow might be helpful:
* Deprecate child methods by adding @deprecated to the method's annotation
* Analyze discovered usages and plan to refactor

