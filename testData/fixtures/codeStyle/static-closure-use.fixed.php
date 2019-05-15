<?php

class ParentClass {
    public function dynamicMethod()      {}
    static public function staticMethod(){}
}

class CasesHolder extends ParentClass {
    private $property;

    public function method() {
        return [
            function() { return $this->property; },
            static function() { return null; },
            static function() { return null; },

            static function() { parent::staticMethod(); },
            function() { parent::dynamicMethod(); },
        ];
    }
}