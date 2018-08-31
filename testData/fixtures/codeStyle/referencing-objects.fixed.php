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