<?php

    $x = & <warning descr="Objects are always passed by reference; please correct '= & new '.">new stdClass()</warning>;
    $x = &<warning descr="Objects are always passed by reference; please correct '= & new '.">new stdClass()</warning>;

    function one   (<warning descr="Objects are always passed by reference; please correct '& $param1'.">stdClass & $param1</warning>, array & $arr, & $str) {}
    function two   (<warning descr="Objects are always passed by reference; please correct '& $param1'.">stdClass &$param1</warning>, array &$arr, &$str) {}
    function three (<warning descr="Objects are always passed by reference; please correct '& $param1'.">stdClass& $param1</warning>, array& $arr, &$str) {}

    interface contract {
        public function method(
            <warning descr="Objects are always passed by reference; please correct '& $param2'.">stdClass & $param2</warning>,
            array & $arr,
            & $str
        );
    }
    abstract class impl implements contract {
        abstract public function method2(
            <warning descr="Objects are always passed by reference; please correct '& $param3'.">stdClass & $param3</warning>,
            array & $arr,
            & $str
        );
    }


    /* false-positives: scalar types */
    function typeHinted(string & $param1, \string & $param2) {}
    function nullableTypeHinted(?string & $param1, ?\string & $param2) {}

    /* false-positives: writing into */
    function writingInto(stdClass &$param) {
        $param = new stdClass();
    }

    /* false-positives: boolean context */
    function booleanContext(stdClass &$one, stdClass &$two, stdClass &$three) {
        if ($one) {}
        while ($two) {}
        do {} while ($three);
    }