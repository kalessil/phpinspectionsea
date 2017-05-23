<?php

function suggestSimplifyingInverted($a) {
    <weak_warning descr="An if-return construct can be replaced with 'return !($a > 0)'.">if</weak_warning> ($a > 0) {
        return false;
    }
    return true;
}

function suggestSimplifying($a) {
    <weak_warning descr="An if-return construct can be replaced with 'return $a > 0'.">if</weak_warning> ($a > 0) {
        return true;
    }
    return false;
}

function suggestSimplifyingReportedCases($a) {
    <weak_warning descr="An if-return construct can be replaced with 'return !(!$a || in_array($a, 'whatever', true))'.">if</weak_warning> (!$a || in_array($a, 'whatever', true)) {
        return false;
    }
    return true;
}

function noFalsePositivesCase($a) {
    if ($a === 0) {
        return false;
    }
    /* comment: ensures that comments are not processed */
    if ($a > 0) {
        return false;
    }
    return true;
}