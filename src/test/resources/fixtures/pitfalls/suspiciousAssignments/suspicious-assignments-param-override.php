<?php

    function overridesParameter($param1, $param2) {
        $param1 = trim($param1);
        <error descr="The parameter is overridden immediately (original value is lost).">$param2</error> = trim($param1);

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