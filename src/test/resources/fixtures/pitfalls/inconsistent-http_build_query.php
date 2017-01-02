<?php

function test () {
    $arr = [];
    <error descr="'ksort($arr, SORT_STRING)' should be used instead, so http_build_query() produces result independent from key types.">ksort($arr)</error>;

    return http_build_query($arr);
}

function test2 () {
    $arr = [];
    ksort($arr, SORT_STRING);

    return http_build_query($arr);
}