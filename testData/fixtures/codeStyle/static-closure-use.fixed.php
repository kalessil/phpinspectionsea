<?php

class ParentClass {
    public function dynamicMethod()      {}
    static public function staticMethod(){}
}

class CasesHolder extends ParentClass {
    private $property;

    public function method() {
        return [
            array_filter([], static function() { return null; }),
            array_filter([], function() { return $this->property; }),
            array_filter([], static function() { return null; }),

            array_filter([], static function() { return parent::staticMethod(); }),
            array_filter([], function() { return parent::dynamicMethod(); }),
        ];
    }

    public function binds_closure() {
        $targetClosure = static function() { ; };
        Closure::bind($targetClosure, null);
        $targetClosure->bindTo(null);

        $skippedClosure = function() { ; };
        Closure::bind($skippedClosure, $this);
        $skippedClosure->bindTo($this);
    }

    public function scoped_factories() {
        return [
            CasesHolder::class => function() { return null; },
            CasesHolder::class => static function() { return null; },
        ];
    }
}

$unscopedFactories = [
    CasesHolder::class => static function() { return null; },
    CasesHolder::class => static function() { return null; },
];