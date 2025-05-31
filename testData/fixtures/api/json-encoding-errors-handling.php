<?php

function cases_holder($encoded, $options) {
    $local = JSON_THROW_ON_ERROR;

    return [
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_decode($encoded)</weak_warning>,
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_decode($encoded, false)</weak_warning>,
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_decode($encoded, false, 512)</weak_warning>,
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_decode($encoded, false, 512, $options)</weak_warning>,
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_decode($encoded, flags: $options)</weak_warning>,

        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_encode($encoded)</weak_warning>,
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_encode($encoded, $options)</weak_warning>,
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_encode($encoded, $options, $depth)</weak_warning>,
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_encode($encoded, flags: $options)</weak_warning>,

        json_decode($encoded, false, 512, $options | JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_PARTIAL_OUTPUT_ON_ERROR),
        json_decode($encoded, false, 512, $local),
        json_decode($encoded, flags: JSON_THROW_ON_ERROR),

        json_encode($encoded, $options | JSON_THROW_ON_ERROR),
        json_encode($encoded, JSON_THROW_ON_ERROR),
        json_encode($encoded, JSON_PARTIAL_OUTPUT_ON_ERROR),
        json_encode($encoded, $local),
        json_encode($encoded, flags: JSON_THROW_ON_ERROR),
    ];
}