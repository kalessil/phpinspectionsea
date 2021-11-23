<?php

    function fix_correctness_arrays() {
        $array = [];

        $x = str_replace([$one, $two, ...$array], [$one, $two, ...$array], '___');

        $y = str_replace([...$array, $one, $two], [...$array, $one, $two], '___');
    }