<?php

class noSet
{
    public function __get($name) // <- reported
    {
        return false;
    }

    public function __isset($name) // <- reported
    {
        return false;
    }

    public function __unset($name) // <- reported
    {
        return false;
    }
}

class noIssetGet
{
    public function __set($name, $value) // <- reported
    {
        return false;
    }
}