<?php

function cases_holder() {
    return [
        array_flip([]),

        array_combine([], array_keys()),
        array_combine([], array_keys(['...'])),
    ];
}