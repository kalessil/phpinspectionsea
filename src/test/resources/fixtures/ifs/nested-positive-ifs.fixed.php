<?php

function cases_holder() {
    if ($a && $b) {
    }

    if ($a && $b) {
    }

    if ($a && $b && $c) {
    }

    if ($a || $b || $c || $d) {
    }

    /* false-positives: mixed out operators */
    if ($a || $b) { if ($c) { } }
}