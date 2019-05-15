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
            function() { return $this->property; },

            function() { parent::dynamicMethod(); },
            static function() { parent::staticMethod(); },
        ];
    }
}