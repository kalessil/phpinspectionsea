<?php

function cases_holder() {
    return [
        isset($a, $b),
        isset($a, $b) && isset($c),
        isset($a, $b) && ($c || $d),

        isset($a) && !isset($b),

        !isset($a, $b),
        !isset($a, $b) || !isset($c),

        isset($a) || !isset($b),
    ];
}