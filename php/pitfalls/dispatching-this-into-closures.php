<?php

class thisAndClosure
{
    public function doSomething()
    {
        return function () use ($this) { // <- reported
            return $this;
        };
    }
}