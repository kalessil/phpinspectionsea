<?php

class ImmediateOverridePropsClass {

    private $legalOverride1;
    private $legalOverride2 = null;

    private $legalOverride3 = [];

    private $legalOverride4 = [];

    private $suspiciousOverride = [];

    public function __construct($x) {
        $this->legalOverride1 = [];
        $this->legalOverride2 = [];

        if ($x) {
            $this->legalOverride3 = [];
        }

        <error descr="The property is overridden immediately (default value is lost).">$this->suspiciousOverride = [];</error>
    }

    public function method() {
        $this->legalOverride4 = [];
    }
}