<?php

class CasesHolder {
    private $property;

    public function method() {
        return [
            function() { return $this->property; },
            function() { return <error descr="'$this' can not be used in static closures.">$this</error>->property; },
        ];
    }
}