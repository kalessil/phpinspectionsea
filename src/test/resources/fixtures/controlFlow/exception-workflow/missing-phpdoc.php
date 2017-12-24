<?php

class CasesHolder {

    private function emitter() {
        if ('' === '-') {
            /* not properly supported ATM */
            $exception = new \LogicException();
            <weak_warning descr="Throws a non-annotated/unhandled exception: '\Exception1'.">throw $exception;</weak_warning>
        }
        throw new \RuntimeException();
    }

    public function trigger() {
        <weak_warning descr="Throws a non-annotated/unhandled exception: '\Exception1'.">$this->emitter();</weak_warning>
    }

}