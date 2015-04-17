<?php

class AClass {
    public $property;

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