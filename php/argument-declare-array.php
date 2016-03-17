<?php

interface IRoot
{
    public function isArray($x = array());            // <- reported
}

interface IChild extends IRoot
{
    public function isArray($x = array());            // <- reported
}

abstract class CRoot
{
    public abstract function resetArray($x = array()); // <- reported
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
