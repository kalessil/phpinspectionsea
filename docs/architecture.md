# Architecture

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

