<?php

    <error descr="[EA] Please use && or || for multiple conditions. Currently no checks are performed after first positive result.">for</error>
        ($i = 0; $i > 0, $i < 10; ++$i) {
    }

    function subRoutine ($param1, $param2) {
        <error descr="[EA] Variable '$param1' is introduced as a function parameter and overridden here.">foreach</error>
            ([] as $param1) {
        }
        <error descr="[EA] Variable '$param2' is introduced as a function parameter and overridden here.">for</error>
            ($param2 = 0;;) {
        }
    }

    for ($forIndex = 0;;) {
        <error descr="[EA] Variable '$forIndex' is introduced in a outer loop and overridden here.">foreach</error>
            ([] as $forIndex) {
        }
        <error descr="[EA] Variable '$forIndex' is introduced in a outer loop and overridden here.">foreach</error>
            ([] as $forIndex => $value) {
        }
    }

    foreach ([] as $foreachIndex => $foreachValue) {
        <error descr="[EA] Variable '$foreachIndex' is introduced in a outer loop and overridden here.">for</error>
            ($foreachIndex = 0;;) {
        }
        <error descr="[EA] Variable '$foreachIndex' is introduced in a outer loop and overridden here.">foreach</error>
            ([] as $foreachIndex => $value) {
        }
    }