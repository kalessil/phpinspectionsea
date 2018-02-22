<?php

class CasesHolder {

    private function emitterOne() {
        if ('' === '-') {
            /* not properly supported ATM */
            $exception = new \LogicException();
            <weak_warning descr="Throws a non-annotated/unhandled exception: '\LogicException'.">throw $exception;</weak_warning>
        } else {
            throw new <weak_warning descr="Throws a non-annotated/unhandled exception: '\RuntimeException'.">\RuntimeException</weak_warning>();
        }
    }

    private function emitterTwo() {
        try {
            if ('' === '-') {
                throw new <weak_warning descr="Throws a non-annotated/unhandled exception: '\RuntimeException'.">\RuntimeException</weak_warning>();
            } else {
                throw new \DomainException();
            }
        } catch (\DomainException $exception) {}

        throw new <weak_warning descr="Throws a non-annotated/unhandled exception: '\UnexpectedValueException'.">\UnexpectedValueException</weak_warning>();
    }

    public function trigger() {
        /* doesn't recognize LogicException */
        $this-><weak_warning descr="Throws a non-annotated/unhandled exception: '\RuntimeException'.">emitterOne</weak_warning>();
        /* doesn't recognize RuntimeException */
        $this-><weak_warning descr="Throws a non-annotated/unhandled exception: '\UnexpectedValueException'.">emitterTwo</weak_warning>();
    }

}