<?php

    $x = & new stdClass();

    function func(stdClass & $param1, array & $arr, & $str) {}

    interface contract {
        public function method(stdClass & $param2, array & $arr, & $str);
    }

    abstract class impl implements contract {
        abstract public function method2(stdClass & $param3, array & $arr, & $str);
    }