<?php

class CasesHolder
{
    abstract function consumer(DateTime $dateTime): void;
    abstract function trigger(): ?DateTime;

    public function scenario(): void
    {
        $this->consumer(<warning descr="Null pointer exception may occur here (result can be null).">$this->trigger()</warning>);
        $this->consumer(<warning descr="Null pointer exception may occur here (result can be null).">null</warning>);
    }
}