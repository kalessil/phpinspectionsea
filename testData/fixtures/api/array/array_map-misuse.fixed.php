<?php

function cases_holder() {
    return [
        array_slice(array_map('...', []), -1),
        array_slice(array_map('...', []), -1, 1),
    ];
}