<?php

class CasesHolder
{
    abstract function scalarConsumer(string $string): void;
    abstract function notNullableConsumer(DateTime $dateTime): void;
    abstract function nullableConsumer(?DateTime $one, DateTime $two = null): void;
    abstract function trigger(): ?DateTime;

    public function scenario(): void
    {
        $this->notNullableConsumer(<warning descr="Null pointer exception may occur here (result can be null).">$this->trigger()</warning>);
        $this->notNullableConsumer(<warning descr="Null pointer exception may occur here (result can be null).">null</warning>);

        $this->nullableConsumer($this->trigger(), $this->trigger());
        $this->nullableConsumer(null, null);

        $this->scalarConsumer(null);
        $this->scalarConsumer($this->trigger());
    }
}