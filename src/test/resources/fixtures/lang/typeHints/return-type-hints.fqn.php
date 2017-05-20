<?php

use MyNamespace\MyClass;
use MyNamespace\MyOtherClass;

class MyLocalClass {
    public function <weak_warning descr="MyClass can be declared as return type hint">fromReturn</weak_warning>()
    { return new MyClass; }

    public function <weak_warning descr="MyClass can be declared as return type hint">fromParam</weak_warning>(MyClass $x)
    { return $x; }

    /** @return MyClass|null */
    public function <weak_warning descr="?MyClass can be declared as return type hint">nullFromDocblock</weak_warning>()
    { return new MyClass; }

    public function <weak_warning descr="?MyClass can be declared as return type hint">nullFromParam</weak_warning>(?MyClass $x)
    { return $x; }
}
