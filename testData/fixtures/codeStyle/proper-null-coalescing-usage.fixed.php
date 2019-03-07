<?php

interface ContractOne {}
interface ContractTwo extends ContractOne{}
interface ContractThree {}

abstract class CasesHolder {
    /** @var CasesHolder|null */
    private $property;

    abstract function method();

    public function cases(ContractOne $one, ContractTwo $two, ContractThree $three) {
        return [
            $this->method(),

            $this->property ?? [],
            $this->property ?? null,

            $one ?? $two,
            $two ?? $one,
            $one ?? $three,
        ];
    }
}