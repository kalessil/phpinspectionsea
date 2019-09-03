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
    function type_declaration_cases_holder(string & $param1, \string & $param2) {}
    function nullable_type_declaration_cases_holder(?string & $param1, ?\string & $param2) {}

    /* false-positives: writing into */
    function value_has_been_written_cases_holder(stdClass &$param) {
        $param = new stdClass();
    }

    /* false-positives: boolean context */
    function boolean_context_cases_holder(stdClass &$one, stdClass &$two, stdClass &$three) {
        if ($one) {}
        while ($two) {}
        do {} while ($three);
    }

    /* false-positives: optional parameters */
    function with_default_value_cases_holder(stdClass &$one = null) {}