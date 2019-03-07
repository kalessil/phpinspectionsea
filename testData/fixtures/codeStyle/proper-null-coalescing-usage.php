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
            <weak_warning descr="It possible to use '$this->method()' instead (reduces cognitive load).">$this->method() ?? null</weak_warning>,

            <weak_warning descr="Resolved operands types are not complimentary, while they should be ([\CasesHolder, null] vs [array]).">$this->property ?? []</weak_warning>,
            $this->property ?? null,

            $one ?? $two,
            $two ?? $one,
            $one ?? $three,
        ];
    }
}