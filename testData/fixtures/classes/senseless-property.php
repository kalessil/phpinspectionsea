<?php

/** @property $docBlockProperty */
class Clazz {
    private <weak_warning descr="'privateUsedAsLocal' property seems to be used as a local variable in 'method' method.">$privateUsedAsLocal</weak_warning>;
    private $privateUsedAsProperty;
    private $privateUnusedDependency;
    protected $protected;
    public $public;

    public function __construct() {
        $this->privateUsedAsProperty = '...';
        $this->privateUnusedDependency = '...';
    }

    public function method() {
        $this->privateUsedAsLocal = $this->privateUsedAsProperty;
        $this->docBlockProperty = $this->privateUsedAsProperty;
        $this->protected = $this->privateUsedAsProperty;
        $this->public = $this->privateUsedAsProperty;
    }
}