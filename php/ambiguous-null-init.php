<?php

class AClass
{
    protected $field1 /*x*/ /*x*/ = /*x*/ /*x*/ null; // <- reported

    protected
        $field2,
        $field3 /*x*/ /*x*/ = /*x*/ /*x*/ null,       // <- reported
        $field4;

    const A_CONST    = null;
}