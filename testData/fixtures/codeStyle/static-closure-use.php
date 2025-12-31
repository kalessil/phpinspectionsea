<?php

class ParentClass {
    public function dynamicMethod()      {}
    static public function staticMethod(){}
}

class CasesHolder extends ParentClass {
    private $property;

    public function method() {
        return [
            array_filter([], <weak_warning descr="[EA] This closure can be declared as static (better scoping; in some cases can improve performance).">function</weak_warning>() { return null; }),
            array_filter([], function() { return $this->property; }),
            array_filter([], static function() { return null; }),
            array_filter([], #[Pure] static function() { return null; }),

            array_filter([], <weak_warning descr="[EA] This closure can be declared as static (better scoping; in some cases can improve performance).">function</weak_warning>() { return parent::staticMethod(); }),
            array_filter([], function() { return parent::dynamicMethod(); }),
        ];
    }

    public function binds_closure() {
        $targetClosure = <weak_warning descr="[EA] This closure can be declared as static (better scoping; in some cases can improve performance).">function</weak_warning>() { ; };
        Closure::bind($targetClosure, null);
        $targetClosure->bindTo(null);

        $skippedClosure = function() { ; };
        Closure::bind($skippedClosure, $this);
        $skippedClosure->bindTo($this);
    }

    public function dispatched_into_static_methods() {
        $targetClosure = <weak_warning descr="[EA] This closure can be declared as static (better scoping; in some cases can improve performance).">function</weak_warning>() { ; };
    	Clazz::method( $targetClosure );

    	$skippedClosure = function() { ; };
    	$object->method( $skippedClosure );
    }

    public function scoped_factories() {
        return [
            CasesHolder::class => function() { return null; },
            CasesHolder::class => static function() { return null; },
        ];
    }
}

$unscopedFactories = [
    CasesHolder::class => <weak_warning descr="[EA] This closure can be declared as static (better scoping; in some cases can improve performance).">function</weak_warning>() { return null; },
    CasesHolder::class => static function() { return null; },
];