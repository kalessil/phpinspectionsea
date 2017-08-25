<?php

    $x = & <warning descr="Objects are always passed by reference; please correct '= & new '.">new stdClass()</warning>;
    function func(<warning descr="Objects are always passed by reference; please correct '& $param1'.">stdClass & $param1</warning>, array & $arr, & $str) {}
    interface contract {
        public function
            method(<warning descr="Objects are always passed by reference; please correct '& $param2'.">stdClass & $param2</warning>, array & $arr, & $str);
    }
    abstract class impl implements contract {
        abstract public function
            method2(<warning descr="Objects are always passed by reference; please correct '& $param3'.">stdClass & $param3</warning>, array & $arr, & $str);
    }


    /* false-positives */
    function typeHinted(string & $param1, \string & $param2) {}