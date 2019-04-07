<?php

function cases_holder() {
    <warning descr="Variable $variable is redundant.">$variable</warning> = $variant;
    $variable = $variable ?? $alternative;

    <warning descr="Variable $variable is redundant.">$variable</warning> = $variant;
    $variable = $variable ?: $alternative;
}