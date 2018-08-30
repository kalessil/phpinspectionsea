<?php

class Clazz
{
    public $property;

    public function & method(&$parameter) {
        $parameter = 'modified';
        return $parameter;
    }

    public function x() {
        return '';
    }

    public static function y() {
        return '';
    }
}

$obj = new Clazz();
$obj->method($obj->property);
$obj->method(<warning descr="Emits a notice (only variable references should be returned by reference).">explode(...[])</warning>);
$obj->method(<warning descr="Emits a notice (only variable references should be returned by reference).">$obj->x()</warning>);
$obj->method(<warning descr="Emits a notice (only variable references should be returned by reference).">Clazz::y()</warning>);

$array = [0];
$obj->method($array[0]);

current($obj->x());
key($obj->x());
