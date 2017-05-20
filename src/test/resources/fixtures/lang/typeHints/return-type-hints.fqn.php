<?php

use MyNamespace\MyClass;
use MyNamespace\MyOtherClass;
use MyNamespace\MyAnotherClass as MyAlias;
use MyNamespace\ManyClassOne as ManyClassOneAlias,
    MyNamespace\ManyClassTwo;

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

    public function <weak_warning descr="ManyClassOneAlias can be declared as return type hint">manyAliasFromReturn</weak_warning>()
    { return new ManyClassOneAlias; }

    public function <weak_warning descr="ManyClassOneAlias can be declared as return type hint">manyAliasFromParam</weak_warning>(ManyClassOneAlias $x)
    { return $x; }

    public function <weak_warning descr="ManyClassTwo can be declared as return type hint">manyFromReturn</weak_warning>()
    { return new ManyClassTwo; }

    public function <weak_warning descr="ManyClassTwo can be declared as return type hint">manyFromParam</weak_warning>(ManyClassTwo $x)
    { return $x; }
}
