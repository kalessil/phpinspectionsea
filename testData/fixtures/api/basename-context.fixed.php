<?php

function test_cases_holder(string $parameter) {
    return [
        basename($parameter, '.ext'),
        basename($parameter, '.ext'),

        str_replace('.ext', ''),
        str_replace('.ext', '.tmp', basename($parameter)),
        str_replace('.ext', '', basename($parameter, '.ext')),
        str_replace('.text.', '', basename($parameter)),
    ];
}