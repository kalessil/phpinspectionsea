<?php

function cases_holder_if_merge() {
    if ($a) {
        if ($b) {}
    } else {
        /* alternative branch case */
    }

    if ($a) {
        if ($b) {}
        else    {}
    }

    if ($a && $b) {
    }

    if ($a && $b && $c) {
    }

    if ($a && $b && $c) {
    }

    if ($a || $b) {
        if ($c || $d) {
        }
    }

    if ($a || $b) {
        if ($c) {
        }
    }

    if ($a) {
        if ($b || $c) {
        }
    }
}

function cases_holder_else_merge() {
    if ($a) {}
    else if ($b) {}

    if ($a) {}
    else if ($b) {}
    else {}
}