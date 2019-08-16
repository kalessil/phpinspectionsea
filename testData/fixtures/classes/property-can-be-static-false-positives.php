<?php


class ParentClass {
    private $private     = [];
    protected $protected = [];
}

class ChildClass extends ParentClass {
}

class CasesHolder extends ChildClass {
    private $private       = ['', '', ''];
    protected $protected   = ['', '', ''];
    public $public         = ['', '', ''];
    static private $static = ['', '', ''];
}