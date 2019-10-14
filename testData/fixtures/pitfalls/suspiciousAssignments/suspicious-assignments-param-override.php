<?php

    function overridesParameter($param1, $param2) {
        $param1 = trim($param1);
        <error descr="[EA] This variable name has already been declared previously without being used.">$param2</error> = trim($param1);

        echo $param1, $param2;
    }

    function cleansParameter($param1, $param2) {
        $param1 = trim($param1);
        $param2 = trim($param2);

        echo $param1, $param2;
    }

    function falsePositivesParameter($param1, &$param2) {
        if ($param2) {
            $param1 = $param2;
        }

        $param2 = '';
    }