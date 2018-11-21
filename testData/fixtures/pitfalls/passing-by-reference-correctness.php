<?php

class Clazz
{
    public $property;

    public function & method(&$parameter) {
        $parameter = '...';
        return $parameter;
    }

    public function x() {
        return '';
    }

    public static function y() {
        return '';
    }
}

function accepts_reference(&$parameter) {
    $parameter = '...';
}

$obj = new Clazz();
$obj->method($obj);
$obj->method($obj->property);
$obj->method(<warning descr="Emits a notice (only variable references should be returned/passed by reference).">explode(...[])</warning>);
$obj->method(<warning descr="Emits a notice (only variable references should be returned/passed by reference).">$obj->x()</warning>);
$obj->method(<warning descr="Emits a notice (only variable references should be returned/passed by reference).">Clazz::y()</warning>);
$obj->method(<warning descr="Emits a notice (only variable references should be returned/passed by reference).">new Clazz()</warning>);

accepts_reference($obj);
accepts_reference($obj->property);
accepts_reference(<warning descr="Emits a notice (only variable references should be returned/passed by reference).">explode(...[])</warning>);
accepts_reference(<warning descr="Emits a notice (only variable references should be returned/passed by reference).">$obj->x()</warning>);
accepts_reference(<warning descr="Emits a notice (only variable references should be returned/passed by reference).">Clazz::y()</warning>);
accepts_reference(<warning descr="Emits a notice (only variable references should be returned/passed by reference).">new Clazz()</warning>);

is_array($obj);
is_array($obj->property);
is_array(explode(...[]));
is_array($obj->x());
is_array(Clazz::y());
is_array(new Clazz());

$array = [0];
$obj->method($array[0]);

current($obj->x());
key($obj->x());
