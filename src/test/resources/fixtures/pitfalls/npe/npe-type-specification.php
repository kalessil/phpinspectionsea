<?php

class CasesHolder {
    function emitter(): ?CasesHolder {}

    function trigger() {
        $one = $this->emitter();
        <warning descr="Null pointer exception may occur here.">$one</warning>->unknownProperty = null;

        /* @var string|null $two */
        $two = $this->emitter();
        <warning descr="Null pointer exception may occur here.">$two</warning>->unknownProperty = null;

        /* @var string $three */
        $three = $this->emitter();
        $three->unknownProperty = null;
    }
}