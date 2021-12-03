<?php

class TypedPropertiesHere {
    private mixed $mixed = null;

    private ?stdClass $property = null;
    private ?stdClass $overridden;

    public function __construct() {
        $this->overridden = null;
    }
}