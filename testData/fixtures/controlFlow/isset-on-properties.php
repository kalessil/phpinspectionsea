<?php

interface ArrayAccessChild extends \ArrayAccess {}

/** @property string $virtualProperty */
class AClass {
    public $property;

    public function api(ArrayAccessChild $obj) {
        return isset($obj['index']);
    }

    public function __get($name) {
        $stdObject = new stdClass();
        if (
            <weak_warning descr="'$this->property !== null' construction should be used instead.">isset($this->property)</weak_warning> ||
            isset($this->$name) ||
            isset($this->virtualProperty) ||

            isset($this->nonExistingProperty) ||

            <weak_warning descr="'$stdObject !== null' construction should be used instead.">isset($stdObject)</weak_warning> ||
            <weak_warning descr="'$stdObject === null' construction should be used instead.">!isset($stdObject)</weak_warning> ||

            isset($stdObject->dynamicProperty)
        ) {
            return '';
        }

        return '';
    }
}
