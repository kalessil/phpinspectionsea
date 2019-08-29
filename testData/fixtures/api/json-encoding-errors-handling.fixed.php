<?php

function cases_holder($encoded, $options) {
    return [
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR | $options),

        json_decode($encoded, false, 512, $options | JSON_THROW_ON_ERROR),
    ];
}