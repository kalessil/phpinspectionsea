<?php

class noGet
{
    public function __set($name, $value)
    {
        return false;
    }
}

class noSet
{
    public function __get($name)
    {
        return false;
    }
}