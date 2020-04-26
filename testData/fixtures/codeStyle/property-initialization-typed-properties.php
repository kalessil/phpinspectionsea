<?php

class TypedPropertiesHere {
    private ?stdClass $property = null;
    private ?stdClass $overridden;

    public function __construct() {
        $this->overridden = null;
    }
}