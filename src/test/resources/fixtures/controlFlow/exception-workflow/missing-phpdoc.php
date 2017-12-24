<?php

class CasesHolder {

    private function emitterOne() {
        if ('' === '-') {
            /* not properly supported ATM */
            $exception = new \LogicException();
            <weak_warning descr="Throws a non-annotated/unhandled exception: '\LogicException'.">throw $exception;</weak_warning>
        } else {
            throw new \RuntimeException();
        }
    }

    private function emitterTwo() {
        try {
            if ('' === '-') {
                <weak_warning descr="Throws a non-annotated/unhandled exception: '\RuntimeException'.">throw new \RuntimeException();</weak_warning>
            } else {
                throw new \DomainException();
            }
        } catch (\DomainException $exception) {}

        throw new \UnexpectedValueException();
    }

    public function trigger() {
        /* doesn't recognize LogicException */
        <weak_warning descr="Throws a non-annotated/unhandled exception: '\RuntimeException'.">$this->emitterOne()</weak_warning>;
        /* doesn't recognize RuntimeException */
        <weak_warning descr="Throws a non-annotated/unhandled exception: '\UnexpectedValueException'.">$this->emitterTwo()</weak_warning>;
    }

}