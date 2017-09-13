<?php

    <error descr="Please use && or || for multiple conditions. Currently no checks are performed after first positive result.">for</error>
        ($i = 0; $i > 0, $i < 10; ++$i) {
    }

    function subRoutine ($param1, $param2) {
        <error descr="Variable '$param1' is introduced as a function parameter and overridden here.">foreach</error>
            ([] as $param1) {
        }
        <error descr="Variable '$param2' is introduced as a function parameter and overridden here.">for</error>
            ($param2 = 0;;) {
        }
    }

    for ($forIndex = 0;;) {
        <error descr="Variable '$forIndex' is introduced in a outer loop and overridden here.">foreach</error>
            ([] as $forIndex) {
        }
        <error descr="Variable '$forIndex' is introduced in a outer loop and overridden here.">foreach</error>
            ([] as $forIndex => $value) {
        }
    }

    foreach ([] as $foreachIndex => $foreachValue) {
        <error descr="Variable '$foreachIndex' is introduced in a outer loop and overridden here.">for</error>
            ($foreachIndex = 0;;) {
        }
        <error descr="Variable '$foreachIndex' is introduced in a outer loop and overridden here.">foreach</error>
            ([] as $foreachIndex => $value) {
        }
    }


    if (empty($wrong) || !empty($correct)) {
        foreach ($correct as $value) {}
        <error descr="A parent condition 'empty($wrong)' looks suspicious.">foreach</error> ($wrong as $value) {}
    }