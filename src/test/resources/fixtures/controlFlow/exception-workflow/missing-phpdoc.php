<?php

class CasesHolder {

    private function emitter() {
        if ('' === '-') {
            /* not properly supported ATM */
            $exception = new \LogicException();
            <weak_warning descr="Throws a non-annotated/unhandled exception: '\LogicException'.">throw $exception;</weak_warning>
        }
        throw new \RuntimeException();
    }

    public function trigger() {
        <weak_warning descr="Throws a non-annotated/unhandled exception: '\RuntimeException'.">$this->emitter();</weak_warning>
    }

}