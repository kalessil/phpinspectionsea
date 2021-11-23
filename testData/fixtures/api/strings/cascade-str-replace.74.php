<?php

    function fix_correctness_arrays() {
        $array = [];

        $x = str_replace([$one, $two], [$one, $two], '___');
        $x = <warning descr="[EA] This str_replace(...) call can be merged with the previous.">str_replace($array, $array, $x)</warning>;

        $y = str_replace($array, $array, '___');
        $y = <warning descr="[EA] This str_replace(...) call can be merged with the previous.">str_replace([$one, $two], [$one, $two], $y)</warning>;
    }