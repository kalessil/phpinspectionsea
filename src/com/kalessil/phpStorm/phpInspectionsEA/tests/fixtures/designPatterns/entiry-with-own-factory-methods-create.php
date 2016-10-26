<?php

final class Entity
{
    private function __construct()
    {
    }

    public static function createFromString()
    {
        return new self();
    }
}