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
            static function() { return <error descr="'$this' can not be used in static closures.">$this</error>->property; },

            static function() { <error descr="Non-static method should not be called statically.">parent::dynamicMethod()</error>; },
            static function() { parent::staticMethod(); },
        ];
    }
}