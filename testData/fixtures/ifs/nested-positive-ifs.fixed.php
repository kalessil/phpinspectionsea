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
    if (($a = 0) && $b) {}

    if (($a ?: 0) && $b) {}

    if (($a ?? 0) && $b) {}
}

function preserve_comments_in_fix() {
    if ($a && $b) {
        // comment one/1
        // comment one/2
        // comment two/1
        // comment two/2
    }

    if ($a) {
        // comment three
    } else if ($b) {
        // comment four/1
        // comment four/2
        // comment five/1
        // comment five/2
    }
}