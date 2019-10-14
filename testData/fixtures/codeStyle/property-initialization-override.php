<?php

class ImmediateOverridePropsClass {

    protected $override0 = [];  // <- non-private, makes sense as the constructor can be bypassed
    private $override1   = [];  // <- reused in the constructor initialization
    private $override2;         // <- properly initialized in the constructor
    private $override3 = [];    // <- override is on 2nd instructions level

    private $suspiciousOverride1
        = <weak_warning descr="[EA] The assignment can be safely removed as the constructor overrides it.">[]</weak_warning>;
    private $suspiciousOverride2 = [];
    private $suspiciousOverride3;

    public function __construct($x) {
        $this->override0 = [];
        $this->override1 = [] + $this->override1;

        $this->override2 = [];

        if ($x) {
            $this->override3 = [];
        }

        $this->suspiciousOverride1 = [[]];
        <weak_warning descr="[EA] Written value is same as default one, consider removing this assignment.">$this->suspiciousOverride2 = [];</weak_warning>
        <weak_warning descr="[EA] Written value is same as default one, consider removing this assignment.">$this->suspiciousOverride3 = null;</weak_warning>
    }

    /* false-positive: override is not in the constructor */
    private $override4 = [];
    public function method() {
        $this->override4 = [];
    }
}


trait TraitWithFields {
    private $private     = [];

    protected $protected = [];
    public $public       = [];
}
class ClassWithTrait {
    use TraitWithFields;
}
class classOverridesTraitFields extends ClassWithTrait {
    private $private = [];

    protected $protected
        = <weak_warning descr="[EA] Written value is same as default one, consider removing this assignment.">[]</weak_warning>;
    public $public
        = <weak_warning descr="[EA] Written value is same as default one, consider removing this assignment.">[]</weak_warning>;
}