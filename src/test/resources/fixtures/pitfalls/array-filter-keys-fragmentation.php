<?php

function cases_holder_index_access() {
    $fragmentedOne = <warning descr="Result keys set might be fragmented, wrapping with 'array_values(...)' is recommended.">array_filter</warning>([], function($v) { return (bool) $v; });
    $fragmentedTwo = <warning descr="Result keys set might be fragmented, wrapping with 'array_values(...)' is recommended.">array_filter</warning>([]);
    
    $defragmented = array_values(array_filter([]));

    return [
        $fragmentedOne[0],
        $fragmentedTwo[1],

        /* false-positives: non-numeric keys */
        $fragmentedOne['...'],
        $fragmentedTwo['...'],

        /* false-positives: de-fragmented arrays */
        $defragmented[0]
    ];
}

function cases_holder_json_encode() {
    $fragmented = <warning descr="Result keys set might be fragmented, wrapping with 'array_values(...)' is recommended.">array_filter</warning>([]);
    return [
        json_encode(<warning descr="Result keys set might be fragmented, wrapping with 'array_values(...)' is recommended.">array_filter</warning>([])),
        json_encode($fragmented)
    ];
}