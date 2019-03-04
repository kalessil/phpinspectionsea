<?php

class CasesHolder {
    private $property;

    public function method() {
        return [
            function() { return $this->property; },
            function() { return $this->property; },
        ];
    }
}