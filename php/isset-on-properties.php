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
            isset($stdObject->property) || isset($this->property) || isset($this->$name) ||
            isset($stdObject) || !isset($stdObject)
        ) {
            return '';
        }

        return '';
    }
}