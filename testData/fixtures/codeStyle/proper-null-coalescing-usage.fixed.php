<?php

abstract class CasesHolder {
    /** @var CasesHolder|null */
    private $property;

    abstract function method();

    public function cases() {
        return [
            $this->method(),

            $this->property ?? [],
            $this->property ?? null,
        ];
    }
}