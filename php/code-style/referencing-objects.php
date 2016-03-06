<?php

    $x = & new stdClass(); // <- reported

    function func(stdClass & $param1, array & $arr, & $str) // <- reported
    {
    }

    interface contract
    {
        public function method(stdClass & $param2, array & $arr, & $str); // <- reported
    }

    abstract class impl implements contract
    {
        abstract public function method2(stdClass & $param3, array & $arr, & $str); // <- reported
    }

    function typeHinted(string & $param1, \string & $param2)
    {
    }