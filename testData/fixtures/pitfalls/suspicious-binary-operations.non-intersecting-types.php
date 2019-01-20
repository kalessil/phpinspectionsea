<?php

abstract class TestCase {
    /** @return int */
    abstract function annotatedReturnsInteger();

    abstract function subject(): float;
    abstract function returnsFloat(): float;
    abstract function returnsInteger(): int;

    public function cases() {
        return [
            $this->subject() === $this->returnsFloat(),
            <error descr="'$this->subject() === $this->returnsInteger()' seems to be always false.">$this->subject() === $this->returnsInteger()</error>,
            $this->subject() == $this->returnsInteger(),
            $this->subject() === $this->annotatedReturnsInteger(),

            $this->subject() !== $this->returnsFloat(),
            <error descr="'$this->subject() !== $this->returnsInteger()' seems to be always true.">$this->subject() !== $this->returnsInteger()</error>,
            $this->subject() != $this->returnsInteger(),
            $this->subject() === $this->annotatedReturnsInteger(),
        ];
    }
}