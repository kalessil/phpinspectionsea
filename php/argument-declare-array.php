<?php

interface IRoot
{
    public function isArray($x = array());
}

interface IChild extends IRoot
{
    public function isArray($x = array());
}

abstract class CRoot
{
    public abstract function resetArray($x = array());
}

class CChild extends CRoot implements IChild
{
    public function isArray($x = array())
    {
    }

    public function resetArray($x = array())
    {
    }
}
