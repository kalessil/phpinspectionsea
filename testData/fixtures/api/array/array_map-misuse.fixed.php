<?php

function cases_holder() {
    return [
        array_map('...', array_slice([], -1)),
        array_map('...', array_slice([], -1, 1)),
    ];
}