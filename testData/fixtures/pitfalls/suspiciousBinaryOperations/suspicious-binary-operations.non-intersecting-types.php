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
            $this->subject() === 0.0,
            <error descr="[EA] '$this->subject() === $this->returnsInteger()' seems to be always false.">$this->subject() === $this->returnsInteger()</error>,
            <error descr="[EA] '$this->subject() === ''' seems to be always false.">$this->subject() === ''</error>,
            <error descr="[EA] '$this->subject() === []' seems to be always false.">$this->subject() === []</error>,
            <error descr="[EA] '$this->subject() === null' seems to be always false.">$this->subject() === null</error>,
            $this->subject() == $this->returnsInteger(),
            $this->subject() === $this->annotatedReturnsInteger(),

            $this->subject() !== $this->returnsFloat(),
            $this->subject() !== 0.0,
            <error descr="[EA] '$this->subject() !== $this->returnsInteger()' seems to be always true.">$this->subject() !== $this->returnsInteger()</error>,
            <error descr="[EA] '$this->subject() !== ''' seems to be always true.">$this->subject() !== ''</error>,
            <error descr="[EA] '$this->subject() !== []' seems to be always true.">$this->subject() !== []</error>,
            <error descr="[EA] '$this->subject() !== null' seems to be always true.">$this->subject() !== null</error>,
            $this->subject() != $this->returnsInteger(),
            $this->subject() === $this->annotatedReturnsInteger(),
        ];
    }
}