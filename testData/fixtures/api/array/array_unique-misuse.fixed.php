<?php

function cases_holder() {
    return [
        array_unique(array_filter([])),
        \array_unique(array_filter([])),
        array_unique(\array_filter([])),

        array_filter(array_unique([]), '....'),
    ];
}