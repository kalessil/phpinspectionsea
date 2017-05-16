<?php

class Clazz
{
    public $property;

    public function & method(&$parameter)
    {
        $parameter = 'modified';
        return $parameter;
    }

    public function x()
    {
        return '';
    }
}

$obj = new Clazz();
$obj->method($obj->property);
$obj->method(<warning descr="Emits a notice (only variable references should be returned by reference)">explode(...[])</warning>);
$obj->method(<warning descr="Emits a notice (only variable references should be returned by reference)">$obj->x()</warning>);

$array = [0];
$obj->method($array[0]);

