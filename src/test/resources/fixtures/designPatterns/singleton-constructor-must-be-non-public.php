<?php

final class <warning descr="Singleton constructor should not be public (normally it's private).">Entity</warning>
{
    public function __construct()
    {
    }

    public static function getInstance()
    {
        return new self();
    }
}