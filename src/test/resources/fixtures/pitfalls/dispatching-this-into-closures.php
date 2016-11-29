<?php

class thisAndClosure
{
    public function doSomething()
    {
        return function () use (<error descr="Will not work. You have to define a temporary variable (e.g. $self) and use it instead.">$this</error>) {
            return $this;
        };
    }
}