<?php

function cases_holder(\SplObjectStorage $storage, array $array) {
    return [
        preg_match('/.../', '...') == -1,  // always false
        preg_match('/.../', '...') == 0,
        preg_match('/.../', '...') == 1,
        preg_match('/.../', '...') == 2,   // always false

        preg_match('/.../', '...') != -1,  // always true
        preg_match('/.../', '...') != 0,
        preg_match('/.../', '...') != 1,
        preg_match('/.../', '...') != 2,   // always true

        preg_match('/.../', '...') === -1,  // always false
        preg_match('/.../', '...') === 0,
        preg_match('/.../', '...') === 1,
        preg_match('/.../', '...') === 2,   // always false

        preg_match('/.../', '...') !== -1,  // always true
        preg_match('/.../', '...') !== 0,
        preg_match('/.../', '...') !== 1,
        preg_match('/.../', '...') !== 2,   // always true

        preg_match('/.../', '...') > -1, // always true
        preg_match('/.../', '...') > 0,
        preg_match('/.../', '...') > 1,  // always false
        preg_match('/.../', '...') > 2,  // always false

        preg_match('/.../', '...') >= -1, // always true
        preg_match('/.../', '...') >= 0,  // always true
        preg_match('/.../', '...') >= 1,  // not error, but no point
        preg_match('/.../', '...') >= 2,  // always false

        preg_match('/.../', '...') < -1, // always false
        preg_match('/.../', '...') < 0,  // always false
        preg_match('/.../', '...') < 1,
        preg_match('/.../', '...') < 2,  // always true

        preg_match('/.../', '...') <= -1, // always false
        preg_match('/.../', '...') <= 0,  // not error, but no point
        preg_match('/.../', '...') <= 1,  // always true
        preg_match('/.../', '...') <= 2,  // always true

        $storage->count() == 0,
        $storage->count() != 0,
        $storage->count() === 0,
        $storage->count() !== 0,
        $storage->count() > 0,
        $storage->count() >= 0, // always true
        $storage->count() < 0,  // always false
        $storage->count() <= 0, // not error, but no point
    ];
}