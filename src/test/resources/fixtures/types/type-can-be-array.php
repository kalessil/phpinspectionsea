<?php

interface IRoot
{
    public function <weak_warning descr="Parameter $x can be declared as 'array $x'.">isArray</weak_warning>($x = array());
}

interface IChild extends IRoot
{
    public function <weak_warning descr="Parameter $x can be declared as 'array $x'.">isArray</weak_warning>($x = array());
}

abstract class CRoot
{
    public abstract function <weak_warning descr="Parameter $x can be declared as 'array $x'.">resetArray</weak_warning>($x = array());
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
