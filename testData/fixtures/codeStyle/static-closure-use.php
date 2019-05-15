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
            <weak_warning descr="This closure can be declared as static (a micro-optimization).">function</weak_warning>() { return null; },
            static function() { return null; },

            <weak_warning descr="This closure can be declared as static (a micro-optimization).">function</weak_warning>() { parent::staticMethod(); },
            function() { parent::dynamicMethod(); },
        ];
    }
}