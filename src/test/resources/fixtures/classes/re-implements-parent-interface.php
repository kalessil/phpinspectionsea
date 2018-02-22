<?php

interface IParent1                                    {}
interface IParent2 extends IParent1                   {}

abstract class AParent1 implements IParent1           {}
abstract class AParent2 implements IParent1, IParent2 {}

class CParent1 extends AParent1
    implements
        <error descr="'\IParent1' is already announced in '\AParent1'.">IParent1</error>
{}

class CParent2 extends AParent2
    implements
        <error descr="'\IParent1' is already announced in '\AParent2'.">IParent1</error>,
        <error descr="'\IParent2' is already announced in '\AParent2'.">IParent2</error>
{}

class ClassImplementsSameInterfaceTwice
    implements
        IParent2,
        <error descr="Class cannot implement previously implemented interface">IParent2</error>
{}
