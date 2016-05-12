<?php

class noSet
{
    public function __isset($name) // <- reported
    {
        return false;
    }
}

class noIsset
{
    public function __set($name, $value) // <- reported
    {
        return false;
    }
}