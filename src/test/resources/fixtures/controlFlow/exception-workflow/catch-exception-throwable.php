<?php

class CasesHolder
{
    /** @throws \RuntimeException */
    abstract function emitter();

    /** */
    public function catchException()
    {
        try {
            $this->emitter();
        } catch (\Exception $exception) {
            ;
        }
    }

    /** */
    public function catchThrowable()
    {
        try {
            $this->emitter();
        } catch (\Throwable $throwable) {
            ;
        }
    }
}