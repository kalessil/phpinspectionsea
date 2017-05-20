<?php

use MyNamespace\MyClass;
use MyNamespace\MyOtherClass;
use MyNamespace\MyAnotherClass as MyAlias;
use MyNamespace\ManyClassOne as ManyClassOneAlias,
    MyNamespace\ManyClassTwo;

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

    public function manyAliasFromReturn(): ManyClassOneAlias
    { return new ManyClassOneAlias; }

    public function manyAliasFromParam(ManyClassOneAlias $x): ManyClassOneAlias
    { return $x; }

    public function manyFromReturn(): ManyClassTwo
    { return new ManyClassTwo; }

    public function manyFromParam(ManyClassTwo $x): ManyClassTwo
    { return $x; }
}

function fromReturn(): MyClass
{ return new MyClass; }

function fromParam(MyClass $x): MyClass
{ return $x; }

/** @return MyClass|null */
function nullFromDocblock(): ?MyClass
{ return new MyClass; }

function nullFromParam(?MyClass $x): ?MyClass
{ return $x; }

function aliasFromReturn(): MyAlias
{ return new MyAlias; }

function aliasFromParam(MyAlias $x): MyAlias
{ return $x; }

function manyAliasFromReturn(): ManyClassOneAlias
{ return new ManyClassOneAlias; }

function manyAliasFromParam(ManyClassOneAlias $x): ManyClassOneAlias
{ return $x; }

function manyFromReturn(): ManyClassTwo
{ return new ManyClassTwo; }

function manyFromParam(ManyClassTwo $x): ManyClassTwo
{ return $x; }
