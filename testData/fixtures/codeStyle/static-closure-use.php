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
            <weak_warning descr="[EA] This closure can be declared as static (better scoping; in some cases can improve performance).">function</weak_warning>() { return null; },
            static function() { return null; },

            <weak_warning descr="[EA] This closure can be declared as static (better scoping; in some cases can improve performance).">function</weak_warning>() { parent::staticMethod(); },
            function() { parent::dynamicMethod(); },
        ];
    }

    public function returns_closure() {
        $closure = function() { ; };
        return $closure;
    }

    public function binds_closure() {
        $closure = function() { ; };
        return Closure::bind($closure, $this);
    }
}