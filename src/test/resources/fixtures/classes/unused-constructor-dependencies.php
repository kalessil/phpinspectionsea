<?php

class ClassUpdSimple
{
    private $p1;
    public function __construct($p1)
    {
        $this-><weak_warning descr="Property is used only in constructor, perhaps we are dealing with dead code here.">p1</weak_warning> = $p1;
    }
}

trait TraitUpd {
    public function getProperty() {
        return $this->traitProperty;
    }
}

class ClassUpdComplex
{
    use TraitUpd;

    private $p1;
    private $p2;
    private $p3;

    private $traitProperty;

    protected $p4;
    public $p5;

    public function __construct($p1, $p2, $p3)
    {
        $this->p1 = $p1;
        $this->p2 = $p2;
        $this-><weak_warning descr="Property is used only in constructor, perhaps we are dealing with dead code here.">p3</weak_warning> =
            ++$this-><weak_warning descr="Property is used only in constructor, perhaps we are dealing with dead code here.">p3</weak_warning>
            +
            $p3;

        $this->traitProperty = 1;

        $this->p4 = 1;
        $this->p5 = 1;
    }

    public function m1() { return [$this->p1]; }
    public function m2() { return [$this->p2]; }
    public function m3() { return [$this->p1, $this->p2]; }
}