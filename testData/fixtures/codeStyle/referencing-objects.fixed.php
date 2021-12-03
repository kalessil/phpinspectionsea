<?php

    $x = new stdClass();
    $x = new stdClass();

    function one   (stdClass $param1, array & $arr, & $str) {}
    function two   (stdClass $param1, array &$arr, &$str) {}
    function three (stdClass $param1, array& $arr, &$str) {}

    interface contract {
        public function method(
            stdClass $param2,
            array & $arr,
            & $str
        );
    }
    abstract class impl implements contract {
        abstract public function method2(
            stdClass $param3,
            array & $arr,
            & $str
        );
    }


    /* false-positives: scalar types */
    function type_declaration_cases_holder(string & $param1, \string & $param2) {}
    function nullable_type_declaration_cases_holder(?string & $param1, ?\string & $param2) {}
    function mixed_type_declaration_cases_holder(mixed & $param1) {}
    function iterable_type_declaration_cases_holder(iterable & $param1) {}

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