<?php

    $x = & <weak_warning descr="Objects are always passed by reference; please correct '= & new '.">new stdClass()</weak_warning>;
    function func(<weak_warning descr="Objects are always passed by reference; please correct '& $param1'.">stdClass & $param1</weak_warning>, array & $arr, & $str) {}
    interface contract {
        public function
            method(<weak_warning descr="Objects are always passed by reference; please correct '& $param2'.">stdClass & $param2</weak_warning>, array & $arr, & $str);
    }
    abstract class impl implements contract {
        abstract public function
            method2(<weak_warning descr="Objects are always passed by reference; please correct '& $param3'.">stdClass & $param3</weak_warning>, array & $arr, & $str);
    }


    /* false-positives */
    function typeHinted(string & $param1, \string & $param2) {}