<?php

function cases_holder($encoded, $options) {
    $local = JSON_THROW_ON_ERROR;

    return [
        json_decode(json: $encoded, flags: JSON_THROW_ON_ERROR),
        json_decode($encoded, flags: JSON_THROW_ON_ERROR),
        json_decode($encoded, false, flags: JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, flags: JSON_THROW_ON_ERROR),
        json_decode($encoded, flags: $options | JSON_THROW_ON_ERROR),
    ];
}