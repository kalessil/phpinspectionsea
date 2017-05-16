<?php

class Clazz
{
    public $property;

    public function & method(&$parameter)
    {
        $parameter = 'modified';
        return $parameter;
    }
}

$obj = new Clazz();
$obj->method(<warning descr="Emits a notice (only variable references should be returned by reference)">$obj->property</warning>);
$obj->method(<warning descr="Emits a notice (only variable references should be returned by reference)">explode(...[])</warning>);

$array = [0];
$obj->method(<warning descr="Emits a notice (only variable references should be returned by reference)">$array[0]</warning>);
