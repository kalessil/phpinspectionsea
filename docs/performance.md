# Static local variables

> Note: the inspection is deactivated by default

Static variables, defined in a function/method are not used widely and in nutshell representing static property visible 
only inside the function/method. As for any static variable, it is stored in memory until script is executed.

From performance point of view the technique is a micro-optimization with following benefits:
- the variable is initialised only once;
- the variable reused on repetitive calls without new memory allocation;
- static variables can be used for replacing/hiding static class members;