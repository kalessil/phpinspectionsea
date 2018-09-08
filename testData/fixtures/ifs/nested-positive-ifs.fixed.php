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

function cases_holder_same_else_merge() {
    if ($a && $b) {} else {}

    if ($a) {
        if ($b) {}
        else {}
    } else { ; }
}

function cases_holder_operations_priority() {
    if (($a = 0) && $b) {
    }

    if (($a ?: 0) && $b) {
    }
}