<?php

use MyNamespace\MyClass;
use MyNamespace\MyOtherClass;
use MyNamespace\MyAnotherClass as MyAlias;

class MyLocalClass {
    public function fromReturn(): MyClass
    { return new MyClass; }

    public function fromParam(MyClass $x): MyClass
    { return $x; }

    /** @return MyClass|null */
    public function nullFromDocblock(): ?MyClass
    { return new MyClass; }

    public function nullFromParam(?MyClass $x): ?MyClass
    { return $x; }

    public function aliasFromReturn(): MyAlias
    { return new MyAlias; }

    public function aliasFromParam(MyAlias $x): MyAlias
    { return $x; }
}
