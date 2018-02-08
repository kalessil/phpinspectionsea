<?php

function cases_holder() {
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
        $defragmented[0],
    ];
}