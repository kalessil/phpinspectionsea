<?php

function suggestSimplifyingInverted($a) {
    return !($a > 0);
}

function suggestSimplifying($a) {
    return $a > 0;
}

function suggestSimplifyingReportedCases($a) {
    return !(!$a || in_array($a, 'whatever', true));
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