<?php

function cases_holder($encoded, $options) {
    $local = JSON_THROW_ON_ERROR;

    return [
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR | $options),

        json_encode($encoded, JSON_THROW_ON_ERROR),
        json_encode($encoded, JSON_THROW_ON_ERROR | $options),
        json_encode($encoded, JSON_THROW_ON_ERROR | $options, $depth),

        json_decode($encoded, false, 512, $options | JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_PARTIAL_OUTPUT_ON_ERROR),
        json_decode($encoded, false, 512, $local),

        json_encode($encoded, $options | JSON_THROW_ON_ERROR),
        json_encode($encoded, JSON_THROW_ON_ERROR),
        json_encode($encoded, JSON_PARTIAL_OUTPUT_ON_ERROR),
        json_encode($encoded, $local),
    ];
}