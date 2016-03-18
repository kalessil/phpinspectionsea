<?php

interface IParent1
{ }
interface IParent2 extends IParent1
{ }

abstract class AParent1 implements IParent1
{ private $x; }
abstract class AParent2 implements IParent1, IParent2
{ private $y; }

class CParent1 extends AParent1 implements IParent1           // <- reported IParent1
{ private $xx; }

class CParent2 extends AParent2 implements IParent1, IParent2 // <- reported IParent1, IParent2
{ private $xx; }
