<?php

/** @property $docBlockProperty */
class Clazz {
    private <weak_warning descr="[EA] 'privateAsLocal' property seems to be used as a local variable in 'method' method.">$privateAsLocal</weak_warning>;
    private $privateAsProperty;

    /** @Id */
    private $privateAnnotated;
    /** @var string */
    private <weak_warning descr="[EA] 'privateTyped' property seems to be used as a local variable in 'method' method.">$privateTyped</weak_warning>;

    private $privateUnusedDependency;
    protected $protected;
    public $public;

    public function __construct() {
        $this->privateAsProperty = '...';
        $this->privateUnusedDependency = '...';
    }

    public function method() {
        $this->privateAsLocal = $this->privateAsProperty;
        $this->privateTyped = $this->privateAsProperty;

        $this->docBlockProperty = $this->privateAsProperty;
        $this->privateAnnotated = $this->privateAsProperty;

        $this->protected = $this->privateAsProperty;
        $this->public = $this->privateAsProperty;
    }
}