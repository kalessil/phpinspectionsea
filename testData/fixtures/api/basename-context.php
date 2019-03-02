<?php

function test_cases_holder(string $parameter) {
    return [
        str_replace('.ext', '', basename($parameter)),
        str_replace('.ext', '', basename($parameter), 1),

        str_replace('.ext', ''),
        str_replace('.ext', '.tmp', basename($parameter)),
        str_replace('.ext', '', basename($parameter, '.ext')),
        str_replace('.text.', '', basename($parameter)),
    ];
}