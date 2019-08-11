<?php

function cases_holder() {
    $variable = $variant ?? $alternative;

    $variable = $variant ?: $alternative;
}

function false_positives_holder() {
    /** @var null|object $variable */
    $variable = $variant;
    $variable = $variable ?? $alternative;

    /** @var null|object $variable */
    $variable = $variant;
    $variable = $variable ?: $alternative;
}