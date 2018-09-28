<?php

function test () {
    $arr = [];
    ksort($arr, SORT_STRING);

    return http_build_query($arr);
}

function test2 () {
    $arr = [];
    ksort($arr, SORT_STRING);

    return http_build_query($arr);
}