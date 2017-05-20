<?php

use MyNamespace\MyClass;
use MyNamespace\MyOtherClass;

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
}
