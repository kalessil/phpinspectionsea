<?php

use MyNamespace\MyClass;
use MyNamespace\MyOtherClass;
use MyNamespace\MyAnotherClass as MyAlias;

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

    public function <weak_warning descr="MyAlias can be declared as return type hint">aliasFromReturn</weak_warning>()
    { return new MyAlias; }

    public function <weak_warning descr="MyAlias can be declared as return type hint">aliasFromParam</weak_warning>(MyAlias $x)
    { return $x; }
}
