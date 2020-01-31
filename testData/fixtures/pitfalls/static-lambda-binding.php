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
            static function() { return <error descr="[EA] '$this' can not be used in static closures.">$this</error>->property; },

            static function() { return function () { return $this->property; }; },

            static function() { <error descr="[EA] Non-static method should not be used in static closures.">parent::dynamicMethod()</error>; },
            static function() { parent::staticMethod(); },
        ];
    }
}