<?php

final class <warning descr="Singleton constructor should be protected">Entity</warning>
{
    private function __construct()
    {
    }

    public static function getInstance()
    {
        return new self();
    }
}