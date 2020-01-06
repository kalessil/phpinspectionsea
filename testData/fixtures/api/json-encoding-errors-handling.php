<?php

function cases_holder($encoded, $options) {
    return [
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_decode($encoded)</weak_warning>,
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_decode($encoded, false)</weak_warning>,
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_decode($encoded, false, 512)</weak_warning>,
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_decode($encoded, false, 512, $options)</weak_warning>,

        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_encode($encoded)</weak_warning>,
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_encode($encoded, $options)</weak_warning>,
        <weak_warning descr="[EA] Please consider taking advantage of JSON_THROW_ON_ERROR flag for this call options.">json_encode($encoded, $options, $depth)</weak_warning>,

        json_decode($encoded, false, 512, $options | JSON_THROW_ON_ERROR),
        json_encode($encoded, $options | JSON_THROW_ON_ERROR),
    ];
}