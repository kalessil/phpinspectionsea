<?php

function cases_holder_array_filter() {
    $fragmentedOne = <warning descr="[EA] Result keys set might be fragmented, wrapping with 'array_values(...)' is recommended.">array_filter</warning>([], function($v) { return (bool) $v; });
    $fragmentedTwo = <warning descr="[EA] Result keys set might be fragmented, wrapping with 'array_values(...)' is recommended.">array_filter</warning>([]);
    
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

function cases_holder_array_unique() {
    $fragmented = <warning descr="[EA] Result keys set might be fragmented, wrapping with 'array_values(...)' is recommended.">array_unique</warning>([]);

    $defragmented = array_values(array_unique([]));

    return [
        $fragmented[0],

        /* false-positives: non-numeric keys */
        $fragmented['...'],

        /* false-positives: de-fragmented arrays */
        $defragmented[0]
    ];
}

function cases_holder_json_encode() {
    $fragmented = <warning descr="[EA] Result keys set might be fragmented, wrapping with 'array_values(...)' is recommended.">array_filter</warning>([]);
    return [
        json_encode(<warning descr="[EA] Result keys set might be fragmented, wrapping with 'array_values(...)' is recommended.">array_filter</warning>([])),
        json_encode($fragmented)
    ];
}