<?php

class ImmediateOverridePropsClass {

    protected $legalOverride0 = [];
    protected $legalOverride1 = [];

    private $legalOverride2;
    private $legalOverride3 = null;

    private $legalOverride4 = [];

    private $legalOverride5 = [];

    private $suspiciousOverride = [];

    public function __construct($x) {
        $this->legalOverride0 = [];
        $this->legalOverride1 = [] + $this->legalOverride1;

        $this->legalOverride2 = [];
        $this->legalOverride3 = [];

        if ($x) {
            $this->legalOverride4 = [];
        }

        <error descr="The property is overridden immediately (default value is lost).">$this->suspiciousOverride = [];</error>
    }

    public function method() {
        $this->legalOverride5 = [];
    }
}