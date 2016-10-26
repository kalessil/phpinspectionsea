<?php

final class Entity
{
    private function __construct()
    {
    }

    public static function fromString()
    {
        return new self();
    }
}