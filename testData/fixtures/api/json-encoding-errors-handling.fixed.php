<?php

class CasesHolder {
    const FLAGS = JSON_THROW_ON_ERROR | JSON_PRETTY_PRINT;

    function method($encoded, $options) {
        define('FLAGS', JSON_THROW_ON_ERROR | JSON_PRETTY_PRINT);

        return [
            json_encode([], FLAGS),
            json_encode([], self::FLAGS),
        ];
    }
}

function cases_holder($encoded, $options) {
    $local = JSON_THROW_ON_ERROR;

    return [
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR),
        json_decode($encoded, false, 512, JSON_THROW_ON_ERROR | $options),
        json_decode($encoded, flags: $options),

        json_encode($encoded, JSON_THROW_ON_ERROR),
        json_encode($encoded, JSON_THROW_ON_ERROR | $options),
        json_encode($encoded, JSON_THROW_ON_ERROR | $options, $depth),
        json_encode($encoded, flags: $options),

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