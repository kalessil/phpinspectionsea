<?php

class classAM1
{
    var $member;   // <- reported
    function m1 {  // <- reported
    }
}

abstract class classAM2
{
    abstract function m1(); // <- reported
}

interface interfaceAM1
{
    function m1(); // <- reported
}