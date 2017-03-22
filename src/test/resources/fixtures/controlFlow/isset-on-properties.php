<?php

interface ArrayAccessChild extends \ArrayAccess {}

class AClass {
    public $property;

    public function api(ArrayAccessChild $obj) {
        return isset($obj['index']);
    }

    public function __get($name) {
        $stdObject = new stdClass();
        if (
            isset($this->property) || isset($this->$name) || isset($this->nonExistingProperty) ||
            isset($stdObject) || !isset($stdObject) || isset($stdObject->property)
        ) {
            return '';
        }

        return '';
    }
}