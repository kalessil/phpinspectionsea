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
            isset(<weak_warning descr="'null !== $this->property' construction should be used instead.">$this->property</weak_warning>) ||
            isset($this->$name) ||
            isset($this->virtualProperty) ||

            isset($this->nonExistingProperty) ||

            isset(<weak_warning descr="'null !== $stdObject' construction should be used instead.">$stdObject</weak_warning>) ||
            !isset(<weak_warning descr="'null === $stdObject' construction should be used instead.">$stdObject</weak_warning>) ||

            isset($stdObject->dynamicProperty)
        ) {
            return '';
        }

        return '';
    }
}