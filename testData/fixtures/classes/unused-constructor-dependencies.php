<?php

class ClassUpdSimple
{
    use TraitUpd;

    private $usedInClass;
    private $usedByTraitOnly;
    private $closureBoundOnly;
    public function __construct($usedInClass, $usedInTrait)
    {
        <weak_warning descr="[EA] Property is used only in constructor, perhaps we are dealing with dead code here.">$this->usedInClass</weak_warning> = $usedInClass;
        $this->usedByTraitOnly = $usedInTrait;
        (function(){ return $this->closureBoundOnly; })();
    }
}

trait TraitUpd {
    public function getProperty() {
        return [$this->traitProperty, $this->usedByTraitOnly];
    }
}

class ClassUpdComplex
{
    use TraitUpd;

    private $p1;
    private $p2;
    private $p3;
    private $usedByLambda;

    private $traitProperty;

    protected $p4;
    public $p5;

    public function __construct($p1, $p2, $p3, $usedByLambda)
    {
        $this->p1 = $p1;
        $this->p2 = $p2;
        <weak_warning descr="[EA] Property is used only in constructor, perhaps we are dealing with dead code here.">$this->p3</weak_warning> =
            max([++$this->p3 + $p3, $p3]);

        $this->traitProperty = 1;

        $this->p4 = 1;
        $this->p5 = 1;

        $this->usedByLambda = $usedByLambda;
    }

    public function m1() { return [$this->p1]; }
    public function m2() { return [$this->p2]; }
    public function m3() { return [$this->p1, $this->p2]; }

    public static function BreakIncapsulation() {
        return function (ClassUpdComplex $instance) {
            return $instance->usedByLambda;
        };
    }
}

/* false-positives: annotated fields */
class ClassWithAnnotations
{
    /** @Id */
    private $property;

    public function __construct($property) {
        $this->property = $property;
    }
}